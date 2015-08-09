package jp.pgw.develop.swallow.saru.apt;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import static javax.tools.Diagnostic.*;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("jp.pgw.develop.swallow.saru.annotation.GenerateHelper")
public class GenerateHelperProcessor extends AbstractProcessor {

    private static final String BASE_NAME = "GenerateHelper";

    private static final Template TEMPLATE;

    static {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(GenerateHelperProcessor.class, "/");
        try {
            TEMPLATE = cfg.getTemplate("meta.ftl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return true;
        }
        for (TypeElement annotation : annotations) {
            for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation))) {
                generate(element);
            }
        }
        return true;
    }

    private void generate(TypeElement element) {
        final Model model = createModel(element);
        final String fqcn = String.format("%s.%s%s", model.getPackageName(), model.getClassName(), BASE_NAME);
        try {
            final JavaFileObject file = processingEnv.getFiler().createSourceFile(fqcn, element);
            try (BufferedWriter writer = new BufferedWriter(file.openWriter())) {
                TEMPLATE.process(model, writer);
                writer.flush();
            }
        } catch (TemplateException | IOException e) {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR, e.getMessage());
        }
    }

    private Model createModel(TypeElement type) {
        final Map<String, PropertyInfo> temp = new HashMap<>();
        final Map<String, PropertyInfo> properties = new LinkedHashMap<>();
        for (ExecutableElement executable : ElementFilter.methodsIn(type.getEnclosedElements())) {
            final Set<Modifier> modifiers = executable.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || !modifiers.contains(Modifier.PUBLIC) || !executable.getParameters().isEmpty()) {
                continue;
            }
            final String methodName = executable.getSimpleName().toString();
            if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
                continue;
            }
            final String propertyName = StringUtils.uncapitalize(methodName.replaceAll("^(get|is)", ""));
            PropertyInfo property = new PropertyInfo();
            property.getter = methodName;
            property.name = propertyName;
            temp.put(propertyName, property);
        }
        for (ExecutableElement executable : ElementFilter.methodsIn(type.getEnclosedElements())) {
            final Set<Modifier> modifiers = executable.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || !modifiers.contains(Modifier.PUBLIC) || executable.getParameters().size() != 1) {
                continue;
            }
            final String methodName = executable.getSimpleName().toString();
            if (!methodName.startsWith("set")) {
                continue;
            }
            final String propertyName = StringUtils.uncapitalize(methodName.replaceAll("^set", ""));
            final PropertyInfo property = temp.remove(propertyName);
            if (property != null) {
                property.setter = methodName;
                properties.put(propertyName, property);
            }
        }
        return new Model(type, properties.values(), processingEnv);
    }

    static class Model {
        final ProcessingEnvironment processingEnv;
        final TypeElement type;
        final Collection<PropertyInfo> properties;

        Model(TypeElement type, Collection<PropertyInfo> properties, ProcessingEnvironment processingEnv) {
            this.type = type;
            this.properties = properties;
            this.processingEnv = processingEnv;
        }

        String getClassName() {
            return type.getSimpleName().toString();
        }

        String getPackageName() {
            return processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
        }

        Collection<PropertyInfo> getProperties() {
            return properties;
        }

    }

    static class PropertyInfo {
        String helperName;
        String getter;
        String setter;
        String name;
    }

}
