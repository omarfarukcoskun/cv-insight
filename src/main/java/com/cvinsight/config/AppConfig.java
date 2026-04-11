package com.cvinsight.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads application configuration.
 *
 * Priority order for ANTHROPIC_API_KEY:
 *   1. Environment variable  ANTHROPIC_API_KEY
 *   2. config.properties file on the classpath
 *
 * To set up locally, either:
 *   a) export ANTHROPIC_API_KEY=sk-ant-...
 *   b) create src/main/resources/config.properties with:
 *        anthropic.api.key=sk-ant-...
 */
public class AppConfig {

    private static final String PROPS_FILE = "config.properties";
    private static final Properties props = loadProperties();

    private AppConfig() {}

    public static String getAnthropicApiKey() {
        // Environment variable takes priority
        String envKey = System.getenv("ANTHROPIC_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }
        String propKey = props.getProperty("anthropic.api.key");
        if (propKey != null && !propKey.isBlank()) {
            return propKey;
        }
        throw new IllegalStateException(
            "Anthropic API key not found. Set the ANTHROPIC_API_KEY environment variable " +
            "or add anthropic.api.key to src/main/resources/config.properties."
        );
    }

    public static String getClaudeModel() {
        return props.getProperty("claude.model", "claude-sonnet-4-6");
    }

    public static int getMaxTokens() {
        return Integer.parseInt(props.getProperty("claude.max_tokens", "1024"));
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is != null) {
                p.load(is);
            }
        } catch (IOException e) {
            System.err.println("Could not load " + PROPS_FILE + ": " + e.getMessage());
        }
        return p;
    }
}
