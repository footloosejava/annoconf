package github.footloosejava.annoconf.usage;

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
public @interface SampleEnumAnnotation {
    SampleEnum value();

    class SampleEnumConfigurableAnnotation implements SampleEnumAnnotation {

        private final SampleEnum value;

        SampleEnumConfigurableAnnotation(SampleEnum value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        @Override
        public SampleEnum value() {
            return value;
        }

        @Override
        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof SampleEnumAnnotation) && value.equals(((SampleEnumAnnotation) o).value());
        }

        @Override
        public String toString() {
            return "@" + SampleEnumAnnotation.class.getName() + "(value=" + value + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return SampleEnumAnnotation.class;
        }
    }
}