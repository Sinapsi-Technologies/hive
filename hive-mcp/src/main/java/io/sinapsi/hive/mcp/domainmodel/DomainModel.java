package io.sinapsi.hive.mcp.domainmodel;

import java.util.List;

public record DomainModel(
        String schemaVersion,
        String context,
        String packageName,
        List<AggregateSpec> aggregates,
        List<UseCaseSpec> useCases) {

    public DomainModel {
        aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
        useCases = useCases == null ? List.of() : List.copyOf(useCases);
    }

    public record AggregateSpec(
            String name,
            IdentifierSpec id,
            List<FieldSpec> fields,
            List<String> invariants,
            List<BehaviorSpec> behaviors,
            List<EventSpec> events) {

        public AggregateSpec {
            fields = fields == null ? List.of() : List.copyOf(fields);
            invariants = invariants == null ? List.of() : List.copyOf(invariants);
            behaviors = behaviors == null ? List.of() : List.copyOf(behaviors);
            events = events == null ? List.of() : List.copyOf(events);
        }
    }

    public record IdentifierSpec(String name, String type) { }

    public record FieldSpec(String name, String type) { }

    public record BehaviorSpec(
            String name,
            String returnType,
            List<String> events,
            String guidance) {

        public BehaviorSpec {
            events = events == null ? List.of() : List.copyOf(events);
        }
    }

    public record EventSpec(String name, List<FieldSpec> fields) {

        public EventSpec {
            fields = fields == null ? List.of() : List.copyOf(fields);
        }
    }

    public record UseCaseSpec(String name, List<FieldSpec> commandFields, boolean factory) {

        public UseCaseSpec {
            commandFields = commandFields == null ? List.of() : List.copyOf(commandFields);
        }
    }
}
