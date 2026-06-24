package io.sinapsi.hive.cli.c4.model;

/**
 * The vocabulary of a Hive hexagonal application. Each constant maps a package convention to a
 * C4 component "type" label, a generic description, a visual style, and whether the element is
 * shown in the (deliberately uncluttered) component diagram.
 *
 * <p>The declaration order is the <em>display order</em>: it walks the hexagon from the outside
 * in and back out — inbound adapter, input port, application service, domain, output port,
 * outbound adapter — so a diagram reads top-to-bottom in the same direction control flows.
 */
public enum ArchitectureElementType {
    INBOUND_ADAPTER("Inbound Adapter", "Drives the application from the outside", true, Style.INBOUND),
    INPUT_PORT("Input Port", "Use case exposed to inbound adapters", true, Style.INPUT_PORT),
    COMMAND("Command", "Immutable input model for a use case", false, Style.NONE),
    APPLICATION_SERVICE("Application Service", "Orchestrates a single use case", true, Style.APPLICATION),
    DOMAIN_AGGREGATE("Domain Aggregate", "Core business rules and invariants", true, Style.DOMAIN),
    DOMAIN_SERVICE("Domain Service", "Domain logic spanning aggregates", true, Style.DOMAIN),
    DOMAIN_MODEL("Domain Model", "Framework-free domain model", true, Style.DOMAIN),
    DOMAIN_ENTITY("Domain Entity", "Domain entity with identity", false, Style.NONE),
    DOMAIN_VALUE_OBJECT("Value Object", "Immutable domain value", false, Style.NONE),
    DOMAIN_EVENT("Domain Event", "Something that happened in the domain", false, Style.NONE),
    DOMAIN_EXCEPTION("Domain Exception", "Domain rule violation", false, Style.NONE),
    DOMAIN_SNAPSHOT("Domain Snapshot", "Serializable view of domain state", false, Style.NONE),
    OUTPUT_PORT("Output Port", "Capability the application requires", true, Style.OUTPUT_PORT),
    OUTBOUND_ADAPTER("Outbound Adapter", "Implements an output port", true, Style.OUTBOUND),
    CONFIGURATION("Configuration", "Wires the module together", true, Style.CONFIGURATION),
    COMMON("Shared/Common", "Shared building block", false, Style.NONE),
    UNKNOWN("Unknown", "Unclassified type", false, Style.NONE);

    private final String label;
    private final String description;
    private final boolean componentVisible;
    private final Style style;

    ArchitectureElementType(String label, String description, boolean componentVisible, Style style) {
        this.label = label;
        this.description = description;
        this.componentVisible = componentVisible;
        this.style = style;
    }

    /** The human label used as the C4 "type" string and as the styling tag name. */
    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    /** Whether this type appears in the C3 component diagram. Domain detail is hidden to stay clean. */
    public boolean componentVisible() {
        return componentVisible;
    }

    /** Stable position in the hexagon flow; relations and elements are sorted by it. */
    public int displayOrder() {
        return ordinal();
    }

    public boolean hasStyle() {
        return style != Style.NONE;
    }

    public String backgroundColor() {
        return style.background;
    }

    public String fontColor() {
        return style.font;
    }

    public String borderColor() {
        return style.border;
    }

    /** Tasteful Hive palette: cool blues for the driving side, purple for the application, warm
     * amber for the domain, greens for the driven side, neutral grey for configuration. */
    private enum Style {
        INBOUND("#E8F3FF", "#0B3D91", "#0B3D91"),
        INPUT_PORT("#EEF7FF", "#1E4E79", "#1E4E79"),
        APPLICATION("#F6F0FF", "#4B2E83", "#4B2E83"),
        DOMAIN("#FFF4E5", "#8A4B00", "#8A4B00"),
        OUTPUT_PORT("#EFFFF4", "#1B5E20", "#1B5E20"),
        OUTBOUND("#F0FFF8", "#006B3C", "#006B3C"),
        CONFIGURATION("#F2F3F5", "#3A3F47", "#8A929E"),
        NONE(null, null, null);

        private final String background;
        private final String font;
        private final String border;

        Style(String background, String font, String border) {
            this.background = background;
            this.font = font;
            this.border = border;
        }
    }
}
