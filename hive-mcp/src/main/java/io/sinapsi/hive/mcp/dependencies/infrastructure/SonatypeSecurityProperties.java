package io.sinapsi.hive.mcp.dependencies.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hive.security.sonatype")
public class SonatypeSecurityProperties {

    private boolean enabled;

    private String baseUrl = "https://api.guide.sonatype.com";

    private String token = "";

    private long timeoutMillis = 5_000;

    private String failSeverity = "HIGH";

    private boolean failOnUnavailable;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public String getFailSeverity() {
        return failSeverity;
    }

    public void setFailSeverity(String failSeverity) {
        this.failSeverity = failSeverity;
    }

    public boolean isFailOnUnavailable() {
        return failOnUnavailable;
    }

    public void setFailOnUnavailable(boolean failOnUnavailable) {
        this.failOnUnavailable = failOnUnavailable;
    }
}
