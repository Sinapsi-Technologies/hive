package ${packageName};

<#list imports as import>
import ${import};
</#list>

public final class ${className} implements ${interfaces} {
<#list methods as method>

    @Override
    public ${method.returnType} ${method.name}(${method.parameters}) {
        ${method.body}
    }
</#list>
}
