package ${packageName};

${importsBlock}

public record ${className}(
<#list fields as field>
    ${field.type} ${field.name}<#sep>,</#sep>
</#list>
) {
<#if factoryMethod?has_content>
${factoryMethod}
</#if>
}
