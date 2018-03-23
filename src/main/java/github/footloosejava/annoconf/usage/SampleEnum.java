package github.footloosejava.annoconf.usage;

import github.footloosejava.annoconf.ConfProps;
import github.footloosejava.annoconf.Configurable;
import github.footloosejava.annoconf.ConfigurableBuilder;

import java.lang.annotation.Annotation;
import java.util.function.Function;

public enum SampleEnum implements Configurable {

    TEST_SPECIAL_NAME("specialName", "testing123", "The TEST_SPECIAL_NAME test name"),
    TEST_STRING("testing123", "The TEST_STRING test name"),
    TEST_NUMBER("123"),
    TEST_NULL(null),
    TEST_BLANK,
    TEST_1("TEST 1 FROM CONFIGURABLE"),
    TEST_2,
    TEST_3;

    private final ConfProps confProps;

    SampleEnum() {
        this(null, null, null);
    }

    SampleEnum(String defaultValue) {
        this(null, defaultValue, null);
    }

    SampleEnum(String defaultValue, String description) {
        this(null, defaultValue, description);
    }

    SampleEnum(String name, String defaultValue, String description) {

        // for enums that wish to have their own Custom Annotation
        Function<String, Annotation> annotationFunction
                = (aName) -> new SampleEnumAnnotation.SampleEnumConfigurableAnnotation(this);

        this.confProps = ConfigurableBuilder.ofEnumAnnotation(annotationFunction, getClass())
                .setName(name == null ? name() : name)
                .setDescription(description)
                .setDefaultValue(defaultValue)
                .build();
    }

    @Override
    public ConfProps getConfProps() {
        return confProps;
    }
}
