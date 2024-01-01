package com.k2view.agent.secrets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SecretManagerImpl implements SecretManager {
    private final Map<String, SecretValue> secrets = new ConcurrentHashMap<>();

    private record SecretValue(String value, long timestamp) {
        SecretValue(String value) {
            this(value, System.currentTimeMillis());
        }
    }

    private final List<SecretManager> secretManagers;

    public SecretManagerImpl(List<SecretManager> secretManagers) {
        this.secretManagers = secretManagers;
        // Set the TTL to 5 minutes
        long ttl = 5;
        TimeUnit timeUnit = TimeUnit.MINUTES;

        // Create a background thread to remove expired entries
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Sleep for the TTL duration
                    timeUnit.sleep(ttl);

                    // Remove expired entries
                    secrets.entrySet().removeIf(entry -> {
                        long age = System.currentTimeMillis() - entry.getValue().timestamp();
                        return age > timeUnit.toMillis(ttl);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    public Optional<String> get(String secretName) {
        return Optional.ofNullable(secrets.computeIfAbsent(secretName, this::load)).map(SecretValue::value);
    }

    private SecretValue load(String key) {
        return secretManagers.stream()
                .map(secretManager -> secretManager.get(key))
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get)
                .map(value -> new SecretValue(value))
                .orElse(null);
    }
}
