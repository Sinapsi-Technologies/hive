package ${packageName};

${importsBlock}
public record ${className}(
<#list fields as field>
    ${field.type} ${field.name}<#sep>,</#sep>
</#list>
) {
    public static ${className} of(${factoryParameters}) {
        return new ${className}(${factoryArguments});
    }
}
