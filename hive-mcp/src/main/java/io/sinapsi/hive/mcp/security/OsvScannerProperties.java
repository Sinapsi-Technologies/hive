package io.sinapsi.hive.mcp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hive.security.osv")
public class OsvScannerProperties {

    private boolean enabled;

    private String executable = "osv-scanner";

    private long timeoutMillis = 120_000;

    private boolean recursive = true;

    private boolean failOnUnavailable;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isFailOnUnavailable() {
        return failOnUnavailable;
    }

    public void setFailOnUnavailable(boolean failOnUnavailable) {
        this.failOnUnavailable = failOnUnavailable;
    }
}
