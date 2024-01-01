package com.k2view.agent.secrets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface SecretManager {
    Optional<String> get(String key);

    static SecretManager INSTANCE = getInstance();

    public static String secretValue(String key){
        return INSTANCE.get(key).orElse("");
    }

    private static SecretManager getInstance() {
        var type = System.getenv("SECRET_MANAGER_TYPE");
        var region = System.getenv("SECRET_MANAGER_REGION");
        return new SecretManagerImpl(init(type, region));
    }
        
    private static List<SecretManager> init(String type, String region) {
        var secretManagers = new ArrayList<SecretManager>();
        secretManagers.add(new EnvSecretManager());
        switch (type) {
            case "AWS" -> secretManagers.add(new AWSSecretManager(System.getenv("SECRET_MANAGER_REGION")));
        }
        return secretManagers;
    }
}
