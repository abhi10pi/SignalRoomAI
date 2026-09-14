package com.example.Backend.ai;

import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.enums.Role;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.UserStatus;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AiProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AiAnalysisLifecycleTest {

    @Autowired
    private SignalRepository signalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiProcessingService aiProcessingService;

    @Autowired
    private com.example.Backend.repository.CommunityAnalysisRepository communityAnalysisRepository;

    @Test
    @Transactional
    void processingSignal_shouldCreateCommunityResearchAndComparisonResults() {
        User user = new User();
        user.setUsername("ai-lifecycle-user");
        user.setEmail("ai-lifecycle-user@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Signal signal = new Signal();
        signal.setAuthor(user);
        signal.setTitle("Should AI processing generate results?");
        signal.setDescription("This signal will exercise the processing lifecycle.");
        signal.setCategory("Technology");
        signal.setStatus(SignalStatus.PROCESSING);
        signal.setDiscussionStart(LocalDateTime.now().minusDays(8));
        signal.setDiscussionEnd(LocalDateTime.now().minusMinutes(1));
        signalRepository.save(signal);

        aiProcessingService.processSignal(signal.getId());

        Signal refreshed = signalRepository.findById(signal.getId()).orElseThrow();
        assertEquals(SignalStatus.CLOSED, refreshed.getStatus());

        var community = communityAnalysisRepository.findBySignalId(signal.getId()).orElseThrow();
        assertNotNull(community.getAnalysisJson());
        assertTrue(community.getAnalysisJson().containsKey("community_summary"));
        assertTrue(community.getAnalysisJson().containsKey("supporting_arguments"));
        assertTrue(community.getAnalysisJson().containsKey("opposing_arguments"));
        assertTrue(community.getAnalysisJson().containsKey("common_arguments"));
        assertTrue(community.getAnalysisJson().containsKey("minority_arguments"));
        assertTrue(community.getAnalysisJson().containsKey("unsupported_claims"));

        assertNotNull(aiProcessingService.getCommunityAnalysis(signal.getId()));
        assertNotNull(aiProcessingService.getResearchAnalysis(signal.getId()));
        assertNotNull(aiProcessingService.getComparisonAnalysis(signal.getId()));
    }
}
