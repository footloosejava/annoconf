package github.footloosejava.annoconf;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Configuration {

    Configuration putDefaults(Configurable[] properties);

    Configuration putDefaults(ConfProps[] properties);

    void freeze();

    Set<String> keys(Configurable[] includeKeys, Configurable[]... more);

    Set<String> keys(List<Configurable> includeKeys);

    String allSettings();

    void put(Configurable configurable);

    void put(Configurable key, String value);

    void putFullyQualifiedName(String fullyQualifiedName, String value);

    /**
     * Provides an iteration over all keys with values.
     *
     * @param consumer the consumer
     */
    void forEach(BiConsumer<String, String> consumer);

    default Stream<Configurable.ConfigurableValue> forAllNonNull(List<Configurable> keys) {
        return keys.stream().map(k -> {
            String value = string(k);
            Configurable.ConfigurableValue cf = null;
            return value == null ? null : new Configurable.ConfigurableValue(k, value);
        }).filter(Objects::nonNull);
    }

    default Stream<Configurable.ConfigurableValue> forAllNonNull(Configurable[] configurables, Configurable[]... moreConfigurables) {
        List<Configurable> list = new ArrayList<>();
        if (configurables != null) {
            Collections.addAll(list, configurables);
        }
        if (moreConfigurables != null) {
            for (Configurable[] ca : moreConfigurables) {
                if (ca != null) {
                    Collections.addAll(list, ca);
                }
            }
        }
        return forAllNonNull(list);
    }


    // CONVENIENCE
    boolean putIfNotNull(Configurable configurable);

    boolean putIfNotNull(Configurable key, String value);


    // FROM Configurable
    String string(Configurable key);

    float floating(Configurable k);

    double doubling(Configurable k);

    int integer(Configurable k);

    boolean booling(Configurable k);


    // FROM FULLY QUALIFIED NAME
    String stringFullyQualifiedName(String k);

    float floatingFullyQualifiedName(String k);

    double doublingFullyQualifiedName(String k);

    int integerFullyQualifiedName(String k);

    boolean boolingFullyQualifiedName(String k);

}
