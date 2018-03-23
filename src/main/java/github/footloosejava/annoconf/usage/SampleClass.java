package github.footloosejava.annoconf.usage;

import github.footloosejava.annoconf.ConfProps;
import github.footloosejava.annoconf.Configurable;
import github.footloosejava.annoconf.ConfigurableBuilder;

public class SampleClass implements Configurable {


    public static final SampleClass FOO
            = new SampleClass("FOO", "The renouned FOO object", "123");

    public static final SampleClass BAR
            = new SampleClass("BAR", "The renouned BAR object", "www.bar.com");

    public static final SampleClass BAZ
            = new SampleClass("BAZ", "", "www.baz.com");

    /////// INNARDS ////////
    private static final SampleClass[] values = {FOO, BAR, BAZ};

    private final String namespace = "usage.SampleClass";
    private final ConfProps confProps;

    private SampleClass(String name, String description, String defaultValue) {
        this.confProps = ConfigurableBuilder.ofClassAnnotation(this.getClass(), namespace)
                .setName(name)
                .setDescription(description)
                .setDefaultValue(defaultValue)
                .build();
    }

    public static SampleClass[] values() {
        return values.clone();
    }

    @Override
    public ConfProps getConfProps() {
        return confProps;
    }
}
