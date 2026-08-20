package com.example.Backend.service;

import com.example.Backend.entity.CredibilityScore;
import com.example.Backend.entity.Domain;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.entity.Validation;
import com.example.Backend.enums.Confidence;
import com.example.Backend.enums.Outcome;
import com.example.Backend.repository.CredibilityScoreRepository;
import com.example.Backend.repository.ValidationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Credibility scoring engine.
 *
 * Final score formula:
 *   accuracy      = correct / total  (Bayesian-smoothed with 2 pseudo-observations at 0.5)
 *   brierPenalty  = avg squared error of confidence-mapped probabilities
 *   streakBonus   = +0.02 per consecutive correct (max +0.10)
 *   volumeFactor  = log10(total + 1) / log10(51)  capped at 1.0  (rewards experience)
 *   rawScore      = accuracy * 0.55
 *                 + (1 - brierPenalty) * 0.30
 *                 + streakBonus * 0.10
 *                 + volumeFactor * 0.05
 *   overconfidencePenalty applied after
 *   finalScore    = clamp(rawScore - overconfidencePenalty, 0, 1)
 */
@Service
public class CredibilityService {

    // Confidence → probability mapping
    private static final double P_LOW     = 0.60;
    private static final double P_MEDIUM  = 0.70;
    private static final double P_HIGH    = 0.85;
    private static final double P_CERTAIN = 0.95;

    private final CredibilityScoreRepository credibilityScoreRepository;
    private final ValidationRepository validationRepository;

    public CredibilityService(CredibilityScoreRepository credibilityScoreRepository,
                               ValidationRepository validationRepository) {
        this.credibilityScoreRepository = credibilityScoreRepository;
        this.validationRepository = validationRepository;
    }

    public void recalculateAfterResolution(Signal signal, Outcome actualOutcome) {
        List<Validation> validations = validationRepository.findBySignalId(signal.getId());
        Domain domain = signal.getDomain();

        // Update submitter score (signal accuracy)
        if (actualOutcome != Outcome.AMBIGUOUS) {
            updateSubmitterScore(signal.getSubmitter(), domain, signal, actualOutcome);
        }

        // Update each consultant's validation score
        for (Validation v : validations) {
            if (actualOutcome == Outcome.AMBIGUOUS) continue;
            boolean correct = v.getPredictedOutcome() == actualOutcome;
            v.setWasCorrect(correct);
            v.setResolvedOutcome(actualOutcome);
            validationRepository.save(v);
            updateConsultantScore(v.getConsultant(), domain, correct, v.getConfidence());
        }
    }

    private void updateSubmitterScore(User user, Domain domain, Signal signal, Outcome actualOutcome) {
        CredibilityScore score = getOrCreate(user, domain);
        score.setTotalSignals(score.getTotalSignals() + 1);

        // For submitters: signal is "correct" if it resolved TRUE (they predicted something would happen)
        // We treat submitting a signal that resolves TRUE as a correct call
        boolean correct = actualOutcome == Outcome.TRUE;
        if (correct) score.setCorrectSignals(score.getCorrectSignals() + 1);

        recalculate(score, validationRepository.findByConsultantIdAndSignalDomainId(
            user.getId(), domain.getId()));
        credibilityScoreRepository.save(score);
    }

    private void updateConsultantScore(User user, Domain domain, boolean correct, Confidence confidence) {
        CredibilityScore score = getOrCreate(user, domain);
        score.setTotalValidations(score.getTotalValidations() + 1);
        if (correct) score.setCorrectValidations(score.getCorrectValidations() + 1);

        // Overconfidence penalty: wrong on HIGH/CERTAIN
        if (!correct && (confidence == Confidence.HIGH || confidence == Confidence.CERTAIN)) {
            BigDecimal penalty = score.getOverconfidencePenalty()
                    .add(new BigDecimal("0.05"))
                    .min(new BigDecimal("0.30"));
            score.setOverconfidencePenalty(penalty);
        }

        recalculate(score, validationRepository.findByConsultantIdAndSignalDomainId(
            user.getId(), domain.getId()));
        credibilityScoreRepository.save(score);
    }

    private void recalculate(CredibilityScore score, List<Validation> allValidations) {
        int totalSignals = score.getTotalSignals();
        int totalValidations = score.getTotalValidations();
        int correctSignals = score.getCorrectSignals();
        int correctValidations = score.getCorrectValidations();
        int total = totalSignals + totalValidations;
        int correct = correctSignals + correctValidations;

        // 1. Bayesian-smoothed accuracy (2 pseudo-observations at 0.5)
        double accuracy = (correct + 1.0) / (total + 2.0);

        // 2. Brier score from validation confidence history
        double brierPenalty = computeBrierPenalty(allValidations);

        // 3. Streak bonus from recent validations
        double streakBonus = computeStreakBonus(allValidations);

        // 4. Volume factor — rewards experience up to 50 predictions
        double volumeFactor = Math.min(Math.log10(total + 1) / Math.log10(51), 1.0);

        // 5. Composite raw score
        double raw = accuracy * 0.55
                + (1.0 - brierPenalty) * 0.30
                + streakBonus * 0.10
                + volumeFactor * 0.05;

        // 6. Apply overconfidence penalty
        double penalty = score.getOverconfidencePenalty().doubleValue();
        double finalScore = Math.max(0.0, Math.min(1.0, raw - penalty));

        score.setAccuracyScore(bd(accuracy));
        score.setFinalScore(bd(finalScore));
        score.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Multiclass Brier score for TRUE, FALSE, and AMBIGUOUS.
     * The confidence is assigned to the predicted class and the remaining
     * probability is split across the other two classes. Lower is better.
     */
    private double computeBrierPenalty(List<Validation> validations) {
        List<Validation> resolved = validations.stream()
                .filter(v -> v.getWasCorrect() != null && v.getResolvedOutcome() != null)
                .toList();
        if (resolved.isEmpty()) return 1.0 / 3.0;

        double sumSquaredError = 0.0;
        for (Validation v : resolved) {
            double predictedProbability = confidenceToProb(v.getConfidence());
            double otherProbability = (1.0 - predictedProbability) / 2.0;

            for (Outcome outcome : Outcome.values()) {
                double probability = outcome == v.getPredictedOutcome()
                        ? predictedProbability : otherProbability;
                double target = outcome == v.getResolvedOutcome() ? 1.0 : 0.0;
                double error = probability - target;
                sumSquaredError += error * error;
            }
        }
        return sumSquaredError / resolved.size();
    }

    /**
     * Streak bonus: +0.02 per consecutive correct at the end of history, max +0.10.
     */
    private double computeStreakBonus(List<Validation> validations) {
        List<Validation> resolved = validations.stream()
                .filter(v -> v.getWasCorrect() != null)
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        int streak = 0;
        for (Validation v : resolved) {
            if (Boolean.TRUE.equals(v.getWasCorrect())) streak++;
            else break;
        }
        return Math.min(streak * 0.02, 0.10);
    }

    private double confidenceToProb(Confidence c) {
        return switch (c) {
            case LOW     -> P_LOW;
            case MEDIUM  -> P_MEDIUM;
            case HIGH    -> P_HIGH;
            case CERTAIN -> P_CERTAIN;
        };
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }

    private CredibilityScore getOrCreate(User user, Domain domain) {
        return credibilityScoreRepository
                .findByUserIdAndDomainId(user.getId(), domain.getId())
                .orElseGet(() -> {
                    CredibilityScore s = new CredibilityScore();
                    s.setUser(user);
                    s.setDomain(domain);
                    return s;
                });
    }
}
