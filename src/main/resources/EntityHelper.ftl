package ${packageName};

/**
 * ${className}.
 */
public final class ${className} {

    /** private constructor. */
    private ${className}() {
    }

    /**
     * create duplicate entity.
     *
     * @param entity base entity
     * @return duplication entity
     */
    public static ${targetClassName} duplicate(${targetClassName} entity) {
      if (entity == null) {
          return null;
      }
      final ${targetClassName} duplication = new ${targetClassName}();
      <#list properties as property>
          <#if property.isIterable>
      if (entity.${property.getter}() != null) {
              <#if property.isSet>
          final java.util.Set<${property.elementType}> elements = new java.util.LinkedHashSet<>();
              <#elseif property.isList>
          final java.util.List<${property.elementType}> elements = new java.util.ArrayList<>();
              <#elseif property.isArray>
          final ${property.elementType}[] elements = new ${property.elementType}[entity.${property.getter}().length];
          int i = 0;
              </#if>
          for (${property.elementType} element : entity.${property.getter}()) {
              <#if property.isArray>
                  <#if property.helperName??>
              elements[i++] = element;
                  <#else>
              elements[i++] = ${helperName}.dup(element);
                  </#if>
              <#else>
                  <#if property.helperName??>
              elements.add(element);
                  <#else>
              elements.add(${helperName}.dup(element));
                  </#if>
              </#if>
          }
          duplication.${property.sesster}(elements);
      }
          <#else>
              <#if property.helperName??>
      duplication.${property.sesster}(entity.${property.getter}());
              <#else>
      duplication.${property.sesster}(${helperName}.dup(entity.${property.getter}()));
              </#if>
          </#if>
      </#list>
      return duplication;
    }

    /**
     * merge entity.
     *
     * @param src source entity
     * @param dest destination entity
     */
    public static void merge(${targetClassName} src, ${targetClassName} dest) {
        if (src == null || dest == null) {
            return;
        }
        <#list properties as property>
            <#if property.isIterable>
        if (entity.${property.getter}() != null) {
        <#if property.isSet>
        final java.util.Set<${property.elementType}> elements = new java.util.LinkedHashSet<>();
        <#elseif property.isList>
        final java.util.List<${property.elementType}> elements = new java.util.ArrayList<>();
        <#elseif property.isArray>
        final ${property.elementType}[] elements = new ${property.elementType}[entity.${property.getter}().length];
        int i = 0;
        </#if>
    for (${property.elementType} element : entity.${property.getter}()) {
        <#if property.isArray>
            <#if property.helperName??>
            elements[i++] = element;
            <#else>
            elements[i++] = ${helperName}.dup(element);
            </#if>
        <#else>
            <#if property.helperName??>
            elements.add(element);
            <#else>
            elements.add(${helperName}.dup(element));
            </#if>
        </#if>
    }
    duplication.${property.sesster}(elements);
    }
    <#else>
        <#if property.helperName??>
        duplication.${property.sesster}(entity.${property.getter}());
        <#else>
        duplication.${property.sesster}(${helperName}.dup(entity.${property.getter}()));
        </#if>
    </#if>
</#list>
return duplication;
}

}
