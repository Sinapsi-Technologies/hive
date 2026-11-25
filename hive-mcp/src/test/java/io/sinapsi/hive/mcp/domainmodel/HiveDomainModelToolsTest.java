package io.sinapsi.hive.mcp.domainmodel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveDomainModelToolsTest {

    @Test
    void rejectsDuplicateFieldsAndUnknownEvents() {
        DomainModel model = new DomainModel("1", "sales", "com.example.sales",
                List.of(new DomainModel.AggregateSpec(
                        "Order",
                        new DomainModel.IdentifierSpec("OrderId", "UUID"),
                        List.of(new DomainModel.FieldSpec("status", "String"),
                                new DomainModel.FieldSpec("status", "String")),
                        List.of(),
                        List.of(new DomainModel.BehaviorSpec("confirm", "Result<Order>",
                                List.of("OrderConfirmedEvent"), "confirm")),
                        List.of())),
                List.of());

        DomainModelValidation result = new DomainModelValidator().validate(model);

        assertFalse(result.valid());
        assertEquals(2, result.errors().size());
    }

    @Test
    void savesOnlyValidModelAsYaml(@TempDir Path project) throws Exception {
        DomainModel model = new DomainModel("1", "sales", "com.example.sales",
                List.of(new DomainModel.AggregateSpec(
                        "Order",
                        new DomainModel.IdentifierSpec("OrderId", "UUID"),
                        List.of(new DomainModel.FieldSpec("status", "String")),
                        List.of("Only DRAFT orders can be confirmed"),
                        List.of(),
                        List.of())),
                List.of());

        Map<String, Object> result = new HiveDomainModelTools(project.toString()).save(model);

        assertEquals(true, result.get("saved"));
        String yaml = Files.readString(project.resolve(".hive/model/domain-model.yml"));
        assertTrue(yaml.contains("schemaVersion: \"1\""));
        assertTrue(yaml.contains("Order"));
    }
}
