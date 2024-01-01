package com.k2view.agent.secrets;

import java.util.Map;
import java.util.Optional;

import com.k2view.agent.Utils;

import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * AWSSecretManager
 */
public class AWSSecretManager implements SecretManager {

    private final SecretsManagerClient client;

    public AWSSecretManager(String region) {
        this.client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(ContainerCredentialsProvider.builder().build())
                .build();
    }

    @Override
    public Optional<String> get(String key) {
        var indexOf = key.indexOf(".");
        if (indexOf == -1) {
            return Optional.empty();
        }
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(key.substring(0, indexOf))
                    .build();

            GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);
            Map<?, ?> result = Utils.gson.fromJson(valueResponse.secretString(), Map.class);

            var val = result.get(key.substring(indexOf + 1));
            return val == null ? Optional.empty() : Optional.ofNullable(val.toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}