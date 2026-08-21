package io.sinapsi.hive.cli.service.inbound;

public enum InboundAdapterType {
    REST("rest", "Controller", "Request"),
    MCP("mcp", "Tool", "Arguments"),
    LISTENER("listener", "Listener", "Message"),
    SCHEDULER("scheduler", "Scheduler", "Trigger");

    private final String directory;
    private final String adapterSuffix;
    private final String transportSuffix;

    InboundAdapterType(String directory, String adapterSuffix, String transportSuffix) {
        this.directory = directory;
        this.adapterSuffix = adapterSuffix;
        this.transportSuffix = transportSuffix;
    }

    public String directory() {
        return directory;
    }

    public String adapterSuffix() {
        return adapterSuffix;
    }

    public String transportSuffix() {
        return transportSuffix;
    }
}
