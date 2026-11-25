package ${packageName};

import io.sinapsi.hive.core.event.DomainEvent;
${importsBlock}

public record ${className}(
<#list fields as field>
    ${field.type} ${field.name}<#sep>,</#sep>
</#list>
) implements DomainEvent {

${factoryMethod}
}
