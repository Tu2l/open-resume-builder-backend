package com.tu2l.user.service;

import com.tu2l.user.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Periodically purges expired auth credentials so {@code user_credentials} cannot grow
 * unbounded. Expiry-based purge (rather than deleting all inactive rows) is deliberate:
 * a rotated-but-unexpired refresh token must survive to power reuse detection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CredentialCleanupJob {

    private final UserCredentialRepository credentialRepository;

    @Scheduled(fixedDelayString = "${app.auth.credential-cleanup-interval-ms:3600000}")
    @Transactional
    public void purgeExpiredCredentials() {
        long removed = credentialRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (removed > 0) {
            log.info("Purged {} expired user credential(s)", removed);
        }
    }
}
