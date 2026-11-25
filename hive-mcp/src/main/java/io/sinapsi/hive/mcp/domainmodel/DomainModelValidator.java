package io.sinapsi.hive.mcp.domainmodel;

import io.sinapsi.hive.mcp.domainmodel.DomainModel.AggregateSpec;
import io.sinapsi.hive.mcp.domainmodel.DomainModel.BehaviorSpec;
import io.sinapsi.hive.mcp.domainmodel.DomainModel.EventSpec;
import io.sinapsi.hive.mcp.domainmodel.DomainModel.FieldSpec;
import io.sinapsi.hive.mcp.domainmodel.DomainModel.UseCaseSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class DomainModelValidator {
    private static final Pattern JAVA_NAME = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final Pattern PACKAGE_NAME = Pattern.compile("[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*");

    public DomainModelValidation validate(DomainModel model) {
        List<String> errors = new ArrayList<>();
        if (model == null) {
            return new DomainModelValidation(false, List.of("model must not be null"));
        }
        if (!"1".equals(model.schemaVersion())) {
            errors.add("schemaVersion must be '1'");
        }
        requireText(model.context(), "context", errors);
        if (model.packageName() == null || !PACKAGE_NAME.matcher(model.packageName()).matches()) {
            errors.add("packageName must be a valid Java package");
        }
        Set<String> aggregateNames = new HashSet<>();
        for (int i = 0; i < model.aggregates().size(); i++) {
            validateAggregate(model.aggregates().get(i), "aggregates[" + i + "]", aggregateNames, errors);
        }
        Set<String> useCaseNames = new HashSet<>();
        for (int i = 0; i < model.useCases().size(); i++) {
            UseCaseSpec useCase = model.useCases().get(i);
            String path = "useCases[" + i + "]";
            validateJavaName(useCase == null ? null : useCase.name(), path + ".name", errors);
            if (useCase != null && useCase.name() != null && !useCaseNames.add(useCase.name())) {
                errors.add(path + ".name duplicates another use case");
            }
            if (useCase != null) {
                validateFields(useCase.commandFields(), path + ".commandFields", errors);
            }
        }
        return new DomainModelValidation(errors.isEmpty(), errors);
    }

    private void validateAggregate(AggregateSpec aggregate, String path, Set<String> names, List<String> errors) {
        if (aggregate == null) {
            errors.add(path + " must not be null");
            return;
        }
        validateJavaName(aggregate.name(), path + ".name", errors);
        if (aggregate.name() != null && !names.add(aggregate.name())) {
            errors.add(path + ".name duplicates another aggregate");
        }
        if (aggregate.id() == null) {
            errors.add(path + ".id is required");
        } else {
            validateJavaName(aggregate.id().name(), path + ".id.name", errors);
            requireText(aggregate.id().type(), path + ".id.type", errors);
        }
        validateFields(aggregate.fields(), path + ".fields", errors);
        Set<String> eventNames = new HashSet<>();
        for (int i = 0; i < aggregate.events().size(); i++) {
            EventSpec event = aggregate.events().get(i);
            String eventPath = path + ".events[" + i + "]";
            if (event == null) {
                errors.add(eventPath + " must not be null");
                continue;
            }
            validateJavaName(event.name(), eventPath + ".name", errors);
            if (event.name() != null && !eventNames.add(event.name())) {
                errors.add(eventPath + ".name duplicates another event");
            }
            validateFields(event.fields(), eventPath + ".fields", errors);
        }
        for (int i = 0; i < aggregate.behaviors().size(); i++) {
            BehaviorSpec behavior = aggregate.behaviors().get(i);
            String behaviorPath = path + ".behaviors[" + i + "]";
            if (behavior == null) {
                errors.add(behaviorPath + " must not be null");
                continue;
            }
            validateJavaName(behavior.name(), behaviorPath + ".name", errors);
            requireText(behavior.returnType(), behaviorPath + ".returnType", errors);
            for (String event : behavior.events()) {
                if (!eventNames.contains(event)) {
                    errors.add(behaviorPath + ".events references unknown event '" + event + "'");
                }
            }
        }
    }

    private void validateFields(List<FieldSpec> fields, String path, List<String> errors) {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            String fieldPath = path + "[" + i + "]";
            if (field == null) {
                errors.add(fieldPath + " must not be null");
                continue;
            }
            validateJavaName(field.name(), fieldPath + ".name", errors);
            requireText(field.type(), fieldPath + ".type", errors);
            if (field.name() != null && !names.add(field.name())) {
                errors.add(fieldPath + ".name duplicates another field");
            }
        }
    }

    private void validateJavaName(String value, String path, List<String> errors) {
        if (value == null || !JAVA_NAME.matcher(value).matches()) {
            errors.add(path + " must be a valid Java identifier");
        }
    }

    private void requireText(String value, String path, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(path + " must not be blank");
        }
    }
}
