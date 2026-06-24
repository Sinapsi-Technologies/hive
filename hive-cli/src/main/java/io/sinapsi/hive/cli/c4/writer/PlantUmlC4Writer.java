package io.sinapsi.hive.cli.c4.writer;

import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import io.sinapsi.hive.cli.c4.model.ArchitectureRelation;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.util.TextNames;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Writes C4-PlantUML diagrams styled with the Hive palette. The component (C3) diagram is the
 * flagship: it draws the hexagon flow with colored element tags and a legend so a reader grasps
 * the architecture in seconds. Container (C2) and context (C1) are higher-altitude summaries
 * derived from the same model.
 */
public final class PlantUmlC4Writer implements DiagramWriter {
    private static final String INCLUDE_BASE =
            "!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/";

    @Override
    public String format() {
        return "puml";
    }

    @Override
    public String fileName(ArchitectureView view) {
        return view.level().filePrefix() + "-" + view.slug() + ".puml";
    }

    @Override
    public String render(ArchitectureView view) {
        return switch (view.level()) {
            case COMPONENT -> renderComponent(view);
            case CONTAINER -> renderContainer(view);
            case CONTEXT -> renderContext(view);
        };
    }

    private String renderComponent(ArchitectureView view) {
        String diagramId = view.level().shortName() + "_" + TextNames.pascalCase(view.slug());
        String boundaryId = view.slug().replace('-', '_') + "_module";
        StringBuilder out = new StringBuilder();

        out.append("@startuml ").append(diagramId).append('\n');
        out.append('\n');
        out.append(INCLUDE_BASE).append(view.level().c4Include()).append('\n');
        out.append('\n');
        appendStyleTags(out, view);
        out.append("title ").append(view.name()).append(" Module - ")
                .append(view.level().shortName()).append(' ').append(view.level().titleSuffix()).append('\n');
        out.append('\n');
        out.append("LAYOUT_WITH_LEGEND()\n");
        out.append('\n');
        out.append("Container_Boundary(").append(boundaryId).append(", \"")
                .append(escape(view.name())).append(" Module\") {\n");
        for (ArchitectureElement element : view.elements()) {
            out.append('\n');
            out.append("    Component(")
                    .append(element.id()).append(", \"")
                    .append(escape(element.name())).append("\", \"")
                    .append(escape(element.type().label())).append("\", \"")
                    .append(escape(element.description())).append('"');
            if (element.type().hasStyle()) {
                out.append(", $tags=\"").append(escape(element.type().label())).append('"');
            }
            out.append(")\n");
        }
        out.append("}\n");
        out.append('\n');
        for (ArchitectureRelation relation : view.relations()) {
            out.append("Rel(").append(relation.sourceId()).append(", ")
                    .append(relation.targetId()).append(", \"")
                    .append(escape(relation.description())).append("\")\n");
        }
        out.append('\n');
        out.append("@enduml\n");
        return out.toString();
    }

    private String renderContainer(ArchitectureView view) {
        String diagramId = view.level().shortName() + "_" + TextNames.pascalCase(view.slug());
        String prefix = view.slug().replace('-', '_');
        boolean inbound = hasAny(view, ArchitectureElementType.INBOUND_ADAPTER);
        boolean application = hasAny(view, ArchitectureElementType.INPUT_PORT, ArchitectureElementType.APPLICATION_SERVICE);
        boolean domain = hasAny(view, ArchitectureElementType.DOMAIN_AGGREGATE,
                ArchitectureElementType.DOMAIN_SERVICE, ArchitectureElementType.DOMAIN_MODEL);
        boolean outbound = hasAny(view, ArchitectureElementType.OUTPUT_PORT, ArchitectureElementType.OUTBOUND_ADAPTER);

        StringBuilder out = new StringBuilder();
        out.append("@startuml ").append(diagramId).append('\n');
        out.append('\n');
        out.append(INCLUDE_BASE).append(view.level().c4Include()).append('\n');
        out.append('\n');
        out.append("title ").append(view.name()).append(" Module - ")
                .append(view.level().shortName()).append(' ').append(view.level().titleSuffix()).append('\n');
        out.append('\n');
        out.append("LAYOUT_WITH_LEGEND()\n");
        out.append('\n');
        out.append("System_Boundary(").append(prefix).append(", \"")
                .append(escape(view.name())).append(" Module\") {\n");
        if (inbound) {
            out.append("    Container(").append(prefix)
                    .append("_inbound, \"Inbound Adapters\", \"Driving side\", \"Turn external input into use-case calls\")\n");
        }
        if (application) {
            out.append("    Container(").append(prefix)
                    .append("_application, \"Application\", \"Use cases\", \"Input ports and services orchestrating use cases\")\n");
        }
        if (domain) {
            out.append("    Container(").append(prefix)
                    .append("_domain, \"Domain\", \"Business rules\", \"Framework-free aggregates and domain logic\")\n");
        }
        if (outbound) {
            out.append("    Container(").append(prefix)
                    .append("_outbound, \"Outbound Adapters\", \"Driven side\", \"Implement output ports against technology\")\n");
        }
        out.append("}\n");
        out.append('\n');
        if (inbound && application) {
            out.append("Rel(").append(prefix).append("_inbound, ").append(prefix).append("_application, \"calls\")\n");
        }
        if (application && domain) {
            out.append("Rel(").append(prefix).append("_application, ").append(prefix).append("_domain, \"uses\")\n");
        }
        if (application && outbound) {
            out.append("Rel(").append(prefix).append("_application, ").append(prefix).append("_outbound, \"uses\")\n");
        }
        out.append('\n');
        out.append("@enduml\n");
        return out.toString();
    }

    private String renderContext(ArchitectureView view) {
        String diagramId = view.level().shortName() + "_" + TextNames.pascalCase(view.slug());
        String prefix = view.slug().replace('-', '_');
        StringBuilder out = new StringBuilder();
        out.append("@startuml ").append(diagramId).append('\n');
        out.append('\n');
        out.append(INCLUDE_BASE).append(view.level().c4Include()).append('\n');
        out.append('\n');
        out.append("title ").append(view.name()).append(" Module - ")
                .append(view.level().shortName()).append(' ').append(view.level().titleSuffix()).append('\n');
        out.append('\n');
        out.append("LAYOUT_WITH_LEGEND()\n");
        out.append('\n');
        out.append("Person(user, \"User\", \"Interacts with the application\")\n");
        out.append("System(").append(prefix).append("_system, \"").append(escape(view.name()))
                .append("\", \"Hive hexagonal application\")\n");
        out.append("Rel(user, ").append(prefix).append("_system, \"uses\")\n");
        for (ArchitectureElement element : view.elements()) {
            if (element.type() == ArchitectureElementType.OUTBOUND_ADAPTER) {
                out.append("System_Ext(").append(element.id()).append("_ext, \"")
                        .append(escape(element.name())).append("\", \"External dependency\")\n");
                out.append("Rel(").append(prefix).append("_system, ").append(element.id())
                        .append("_ext, \"uses\")\n");
            }
        }
        out.append('\n');
        out.append("@enduml\n");
        return out.toString();
    }

    private void appendStyleTags(StringBuilder out, ArchitectureView view) {
        Set<ArchitectureElementType> types = new LinkedHashSet<>();
        for (ArchitectureElement element : view.elements()) {
            if (element.type().hasStyle()) {
                types.add(element.type());
            }
        }
        if (types.isEmpty()) {
            return;
        }
        List<ArchitectureElementType> ordered = new ArrayList<>(types);
        ordered.sort((a, b) -> Integer.compare(a.displayOrder(), b.displayOrder()));
        out.append("' Hive architecture styling\n");
        for (ArchitectureElementType type : ordered) {
            out.append("AddElementTag(\"").append(escape(type.label())).append("\", $bgColor=\"")
                    .append(type.backgroundColor()).append("\", $fontColor=\"")
                    .append(type.fontColor()).append("\", $borderColor=\"")
                    .append(type.borderColor()).append("\")\n");
        }
        out.append('\n');
    }

    private boolean hasAny(ArchitectureView view, ArchitectureElementType... types) {
        for (ArchitectureElement element : view.elements()) {
            for (ArchitectureElementType type : types) {
                if (element.type() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    private String escape(String value) {
        return value.replace("\"", "'");
    }
}
