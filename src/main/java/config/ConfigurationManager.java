package config;

import org.aeonbits.owner.ConfigCache;

public class ConfigurationManager {
    public static Configuration config() {
        return ConfigCache.getOrCreate(Configuration.class);
    }
}
