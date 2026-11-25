package ${packageName};

${importsBlock}

public final class ${className} {

    private final ${idType} id;
<#list fields as field>
    private ${field.type} ${field.name};
</#list>

    public ${className}(${constructorParameters}) {
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

${factoryMethod}
}
