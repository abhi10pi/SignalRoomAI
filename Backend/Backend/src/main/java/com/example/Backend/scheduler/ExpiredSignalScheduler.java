package com.example.Backend.scheduler;

import com.example.Backend.entity.Signal;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.repository.SignalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiredSignalScheduler {

    private final SignalRepository signalRepository;

    public ExpiredSignalScheduler(SignalRepository signalRepository) {
        this.signalRepository = signalRepository;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.expired-signals-delay-ms:60000}")
    @Transactional
    public void markExpiredSignals() {
        List<Signal> expiredSignals = signalRepository.findByStatusInAndResolutionDateBefore(
                List.of(SignalStatus.PENDING_VALIDATION, SignalStatus.VALIDATED),
                LocalDateTime.now());

        for (Signal signal : expiredSignals) {
            signal.setStatus(SignalStatus.EXPIRED_UNRESOLVED);
        }

        if (!expiredSignals.isEmpty()) {
            signalRepository.saveAll(expiredSignals);
        }
    }
}