package ${packageName};

<#list imports as import>
import ${import};
</#list>

public record ${className}(
<#list fields as field>
    ${field.type} ${field.name}<#sep>,</#sep>
</#list>
) {
}
