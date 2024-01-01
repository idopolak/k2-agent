package com.k2view.agent.secrets;

import java.util.Optional;

public class EnvSecretManager implements SecretManager {

    @Override
    public Optional<String> get(String secretName) {
        return Optional.ofNullable(System.getenv(secretName));
    }
}
