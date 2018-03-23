# annoconf - the Friendly way to Combine Annotations and Configurations

Quickly create injectable properties from both Enums and Classes with built-in Annotation support.

The included INIConfiguration allows for querying for values from INI files as well as manually set properties in your program. 

This was designed to work well with Guice and other dependency injection frameworks and then have automatic type conversion for constants!

## Enums that extend Configurable

```
// USE A CUSTOM ANNOTATION
@Foo(FooBar.TEST_NUMBER);
int foo;
```

## Classes that extend Configurable

```// USE A BUILT IN GENERIC "Conf" annotation
@Conf(type = SampleClassFooBar.class, key = "FOO")
int foo;
```

... or like this with a fully qualified name:


```
@Named("usage.SampleClassFooBar-FOO")
int foo;
```

... or like this:


```
String value = configuration.string(SampleClassFooBar.FOO);
```

... or like this:

```
String value = configuration.stringFullyQualifiedName("usage.SampleClassFooBar-FOO");
```

... or like .. Well you get the idea!


By default, INIConfiguration ensures that programmatically set values override INI file values.

Classes are easy to extend and build additional functionality on.

Feel free to contribute!


To get this Git project into your build:

Follow instructionm here https://jitpack.io/#footloosejava/annoconf or these steps for Maven:

Step 1. Add the JitPack repository to your build file

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Step 2. Add the dependency

```
<dependency>
    <groupId>com.github.footloosejava</groupId>
    <artifactId>annoconf</artifactId>
    <version>v1.0</version>
</dependency>
```

***

## Setting up a Guice injection is this easy ...

Objects can be injected at the Field level, or on methods or the constructor.


```
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
```
