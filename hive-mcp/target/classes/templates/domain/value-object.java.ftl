package ${packageName};

<#if importsBlock?has_content>
${importsBlock}
</#if>

public record ${className}(${valueType} value) {
<#if notNull || notBlank || min?has_content || max?has_content || minLength?has_content || maxLength?has_content || pattern?has_content>
    public ${className} {
<#if notNull && notBlank>
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
<#elseif notNull>
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
<#elseif notBlank>
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
</#if>
<#if minLength?has_content>
        if (value.length() < ${minLength}) {
            throw new IllegalArgumentException("value length must be at least ${minLength}");
        }
</#if>
<#if maxLength?has_content>
        if (value != null && value.length() > ${maxLength}) {
            throw new IllegalArgumentException("value length must be at most ${maxLength}");
        }
</#if>
<#if pattern?has_content>
        if (value == null || !value.matches("${pattern}")) {
            throw new IllegalArgumentException("value must match pattern");
        }
</#if>
<#if min?has_content>
        if (value.compareTo(${min}) < 0) {
            throw new IllegalArgumentException("value must be at least ${min}");
        }
</#if>
<#if max?has_content>
        if (value.compareTo(${max}) > 0) {
            throw new IllegalArgumentException("value must be at most ${max}");
        }
</#if>
    }
</#if>
<#if factoryMethod?has_content>

${factoryMethod}
</#if>

}
