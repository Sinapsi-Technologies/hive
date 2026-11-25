package ${packageName};

${importsBlock}
import io.sinapsi.hive.core.domain.AggregateRoot;
import io.sinapsi.hive.core.event.DomainEvent;
import java.util.List;

public final class ${className} implements AggregateRoot<${idType}> {

    private final ${idType} id;

<#list fields as field>
    private ${field.type} ${field.name};
</#list>

    private ${className}(${constructorParameters}) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
<#list fields as field>
        this.${field.name} = ${field.name};
</#list>
    }

    public ${idType} getId() {
        return id;
    }

${createMethod}
${rehydrateMethod}

    @Override
    public List<DomainEvent> pullDomainEvents() {
        return List.of();
    }
}
