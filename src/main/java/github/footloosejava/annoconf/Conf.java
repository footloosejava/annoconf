package github.footloosejava.annoconf;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Objects;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@BindingAnnotation
public @interface Conf {

    Class<? extends Configurable> type();

    String key();

    class ConfAnnotation implements Conf {

        private final Class<? extends Configurable> type;
        private final String key;

        public ConfAnnotation(Class<? extends Configurable> type, String key) {
            this.type = Objects.requireNonNull(type, "type");
            this.key = Objects.requireNonNull(key, "key");

        }

        @Override
        public Class<? extends Configurable> type() {
            return type;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return ((127 * "type".hashCode()) ^ type.hashCode()) + ((127 * "key".hashCode()) ^ key.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Conf) {
                Conf other = (Conf) o;
                return type.equals(other.type()) && key.equals(other.key());
            }
            return false;
        }

        @Override
        public String toString() {
            return "@" + Conf.class.getName() + "(type=" + type + ", key=" + key + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Conf.class;
        }
    }
}

