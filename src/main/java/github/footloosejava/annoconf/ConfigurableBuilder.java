package github.footloosejava.annoconf;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;

public class ConfigurableBuilder implements ConfProps, Configurable {

    private final String namespace;
    private final String name;
    private final String description;
    private final String defaultValue;

    private final String fullyQualifiedName;
    private final Annotation annotation;

    /**
     * @param namespace    the namespace
     * @param name         the name (the part after the last namespace separator)
     * @param description  nullable
     * @param defaultValue nullable
     * @param annotation   the annotation
     */
    public ConfigurableBuilder(String namespace,
                               String name,
                               String description,
                               String defaultValue,
                               Annotation annotation
    ) {
        this.namespace = namespace.trim();
        this.name = name.trim();
        this.description = Objects.toString(description, "").trim();
        this.defaultValue = defaultValue;
        this.fullyQualifiedName = this.namespace.isEmpty() ? this.name : this.namespace + "-" + this.name;
        this.annotation = Objects.requireNonNull(annotation, "Annotation cannot be null");
    }

    public static ConfBuilder ofEnumAnnotation(Function<String, Annotation> annotationMaker, Class classNameSpace) {
        return ofEnumAnnotation(annotationMaker, classNameSpace.getName());
    }

    public static ConfBuilder ofEnumAnnotation(Function<String, Annotation> annotationMaker, String nameSpace) {
        return new ConfBuilder(annotationMaker, nameSpace);
    }

    public static ConfBuilder ofClassAnnotation(Class<? extends Configurable> target) {
        return ofClassAnnotation(target, target.getName());
    }

    public static ConfBuilder ofClassAnnotation(Class<? extends Configurable> target, String nameSpace) {
        return new ConfBuilder(name -> new Conf.ConfAnnotation(target, name), nameSpace);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ConfProps) {
            ConfProps that = (ConfProps) o;
            return namespace.equals(that.getNameSpace()) &&
                    name.equals(that.getName()) &&
                    description.equals(that.getDescription()) &&
                    annotation.equals(that.getAnnotation()) &&
                    Objects.equals(getDefaultValue(), that.getDefaultValue());
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getNameSpace(), getName(), getDescription(), getDefaultValue(), getAnnotation());
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final String getNameSpace() {
        return namespace;
    }

    @Override
    public final String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public final Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public final String getValue(Configuration configuration) {
        String v = configuration.string(this);
        return v == null ? getDefaultValue() : v;
    }

    @Override
    public final ConfProps getConfProps() {
        return this;
    }

    public static class ConfBuilder {

        private final String namespace;
        private final Function<String, Annotation> annotationBiFunction;
        private String name;
        private String description;
        private String defaultValue;

        private ConfBuilder(Function<String, Annotation> annotationBiFunction, String namespace) {
            Objects.requireNonNull(namespace, "namespace can be blank but not null");
            Objects.requireNonNull(annotationBiFunction, "the annotation generator cannot be null.");
            this.namespace = namespace.trim();
            this.annotationBiFunction = annotationBiFunction;
        }


        public ConfBuilder setName(String name) {
            this.name = name == null ? null : name.trim();
            return this;
        }

        public ConfBuilder setName(Enum<?> anEnum) {
            this.name = anEnum.name();
            return this;
        }

        public ConfBuilder setDescription(String description) {
            this.description = Objects.toString(description, "").trim();
            return this;
        }

        public ConfBuilder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ConfigurableBuilder build() {
            Objects.requireNonNull(this.name, "Name must be set!");
            Annotation annotation = annotationBiFunction.apply(this.name);
            return new ConfigurableBuilder(namespace, name, description, defaultValue, annotation);
        }
    }
}
