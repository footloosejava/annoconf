package github.footloosejava.annoconf.usage;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import github.footloosejava.annoconf.Conf;
import github.footloosejava.annoconf.Configuration;
import github.footloosejava.annoconf.INIConfiguration;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.nio.file.Paths;

public class ConfigurableExample extends AbstractModule {

    private final Configuration configuration;

    public ConfigurableExample(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void configure() {
        // Optional - Bind Configuration class to the instance
        bind(Configuration.class).toInstance(configuration);

        // BIND ALL THE DEFAULT VALUES AS ANNOTATIONS
        configuration.forAllNonNull(SampleEnum.values(), SampleClass.values())
                .forEach(confProp -> bindConstant().annotatedWith(confProp.annotation()).to(confProp.value()));

        // A NICE EXTRA - Bind Non Null values as regular Named keys for FULLY QUALIFIED NAMES
        configuration.forEach((k, v) -> bindConstant().annotatedWith(Names.named(k)).to(v));
    }


    static class TestClass {

        private final String name;

        @Inject
        public TestClass(@SampleEnumAnnotation(SampleEnum.TEST_NUMBER) String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {


        // locate INI file
        String filename = Paths.get("src", "test", "resources", "test.ini").toString();

        // (1) Get configuration
        // (2) If needed, add INI namespace mappings
        // (3) Populate configuration with default values
        Configuration configuration = new INIConfiguration(filename)
                .addNamespaceToINIMapping("github.footloosejava.annoconf.usage.SampleEnum", "SampleEnum")
                .addNamespaceToINIMapping("usage.SampleClass", "SampleClass")
                .putDefaults(SampleEnum.values())
                .putDefaults(SampleClass.values());

        // get
        ConfigurableExample module = new ConfigurableExample(configuration);
        Injector injector = Guice.createInjector(module);


        Object objectWithInjectableFields = new Object() {

            // SampleEnum - USE ANNOTATIONS
            @Inject
            @SampleEnumAnnotation(SampleEnum.TEST_NUMBER)
            int enumFooBarTestNumberByAnnotation;

            // SampleEnum - Or use the FULLY QUALIFIED NAME
            @Inject
            @Named("github.footloosejava.annoconf.usage.SampleEnum-TEST_NUMBER")
            int enumFooBarTestNumberByFQN;


            // SampleClass - USE ANNOTATIONS
            @Inject
            @Conf(type = SampleClass.class, key = "FOO")
            int classFooBarFOONumberByConfAnnotation;

            // SampleClass - Or use the FULLY QUALIFIED NAME
            @Inject
            @Named("usage.SampleClass-FOO")
            int classFooBarFOONumberByFQN;

        };

        System.out.println("LETS SEE THE CONFIGURATION VALUES INJECTED ... \n\n\n");

        // INJECT MEMBERS OF INNER Object Class
        injector.injectMembers(objectWithInjectableFields);
        System.out.println(ReflectionToStringBuilder.toString(objectWithInjectableFields, ToStringStyle.MULTI_LINE_STYLE));


        // INJECT NEW INSTANCE of TEST CLASS
        TestClass classWithInjectableConstructor = injector.getInstance(TestClass.class);
        System.out.println(ReflectionToStringBuilder.toString(classWithInjectableConstructor, ToStringStyle.MULTI_LINE_STYLE));
    }
}
