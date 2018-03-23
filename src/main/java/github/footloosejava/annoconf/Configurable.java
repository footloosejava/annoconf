package github.footloosejava.annoconf;

import java.lang.annotation.Annotation;
import java.util.Objects;

public interface Configurable {

    class ConfigurableValue {
        private final Configurable configurable;
        private final String value;

        public ConfigurableValue(Configurable configurable, String value) {
            Objects.requireNonNull(configurable);
            Objects.requireNonNull(value);
            this.configurable = configurable;
            this.value = value;
        }

        public Configurable configurable() {
            return configurable;
        }

        public ConfProps confProps() {
            return configurable.getConfProps();
        }

        public Annotation annotation() {
            return configurable.getConfProps().getAnnotation();
        }

        public String defaultValue() {
            return configurable.getConfProps().getDefaultValue();
        }

        public String namespace() {
            return configurable.getConfProps().getNameSpace();
        }

        public String name() {
            return configurable.getConfProps().getName();
        }

        public String value() {
            return value;
        }
    }

    ConfProps getConfProps();
}
