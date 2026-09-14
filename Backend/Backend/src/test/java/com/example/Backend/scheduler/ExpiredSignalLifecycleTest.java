package com.example.Backend.scheduler;

import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.enums.Role;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.UserStatus;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ExpiredSignalLifecycleTest {

    @Autowired
    private SignalRepository signalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpiredSignalScheduler expiredSignalScheduler;

    @Test
    @Transactional
    void expiredSignal_shouldMoveToProcessingBeforeClosure() {
        User user = new User();
        user.setUsername("scheduler-user");
        user.setEmail("scheduler-user@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Signal signal = new Signal();
        signal.setAuthor(user);
        signal.setTitle("Expired signal");
        signal.setDescription("This should move to processing when expired.");
        signal.setCategory("Technology");
        signal.setStatus(SignalStatus.OPEN);
        signal.setDiscussionStart(LocalDateTime.now().minusDays(8));
        signal.setDiscussionEnd(LocalDateTime.now().minusMinutes(1));
        signalRepository.save(signal);

        expiredSignalScheduler.markExpiredSignals();

        Signal updated = signalRepository.findById(signal.getId()).orElseThrow();
        assertEquals(SignalStatus.PROCESSING, updated.getStatus());
    }
}
