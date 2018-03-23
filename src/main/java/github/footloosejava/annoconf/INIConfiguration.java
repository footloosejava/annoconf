package github.footloosejava.annoconf;

import org.apache.commons.lang3.StringUtils;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

/**
 * Uses an INI file to load values as well as those that can be manually set.
 */
public class INIConfiguration implements Configuration {


    private static final Logger log = LoggerFactory.getLogger(INIConfiguration.class);

    private static final String DEFAULT_NAMESPACE_SEPARATOR = "-";

    private final Map<String, String> conf = new ConcurrentHashMap<>();
    private final Map<String, String> namespaceToINISection = Collections.synchronizedMap(new LinkedHashMap<>());
    private final String namespaceSeparator;
    private final Ini ini;
    private final Preferences prefs;

    private volatile boolean frozen;

    public INIConfiguration(String confFile) {
        this(DEFAULT_NAMESPACE_SEPARATOR, confFile);
    }

    public INIConfiguration(Configurable confileKey) {
        this(DEFAULT_NAMESPACE_SEPARATOR, confileKey.getConfProps().getDefaultValue());
    }

    public INIConfiguration(String nameSpaceSeperator, Configurable confFile) {
        this(nameSpaceSeperator, confFile.getConfProps().getDefaultValue());
    }

    /**
     * @param defaultNameSpaceSeperator NameSpace seperator must be specified.
     * @param confFile                  The 'confFile' parameter does not have to exist. However, it cannot be null.
     */
    public INIConfiguration(String defaultNameSpaceSeperator, String confFile) {
        this.namespaceSeparator = Objects.requireNonNull(defaultNameSpaceSeperator, "NameSpace seperator must be specified.");
        if (confFile == null || confFile.isEmpty()) {
            throw new IllegalArgumentException("The 'confFile' parameter does not have to exist. However, it cannot be null.");
        }
        File file = new File(confFile);
        if (file.exists()) {
            try {
                ini = new Ini(file);
            } catch (IOException e) {
                log.error("Exception loading INI file '" + confFile + "': reason= {}", e.getMessage());
                throw new UncheckedIOException(e);
            }
            prefs = new IniPreferences(ini);
        } else {
            ini = null;
            prefs = null;
        }
    }


    public INIConfiguration addNamespaceToINIMapping(String namespace, String iniSection) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(iniSection);
        namespaceToINISection.putIfAbsent(namespace, iniSection);
        return this;
    }

    /**
     * For each Configurable, the defaults will be loaded into the configuration.
     * The order of loading matters because the value will override any previous values matching the same key.
     *
     * @param properties an array of Configurable objects
     */
    @Override
    public INIConfiguration putDefaults(Configurable[] properties) {
        ConfProps[] confProps = Stream.of(properties).map(Configurable::getConfProps)
                .toArray(ConfProps[]::new);
        return putDefaults(confProps);
    }

    @Override
    public INIConfiguration putDefaults(ConfProps[] properties) {
        for (ConfProps prop : properties) {
            put(prop.getFullyQualifiedName(), prop.getDefaultValue(), true);
        }
        return this;
    }

    public boolean iniLoaded() {
        return ini != null && prefs != null;
    }

    @Override
    public void freeze() {
        frozen = true;
    }

    @Override
    public Set<String> keys(List<Configurable> includeKeys) {
        // ADD WHATEVER IS IN CONF MAP - may contain unknown keys
        Set<String> keys = new TreeSet<>(conf.keySet());
        if (ini != null) {
            try {
                // make reverse map - first values take precedence
                Map<String, String> sectionToNameSpace = new LinkedHashMap<>();
                namespaceToINISection.forEach((k, v) -> sectionToNameSpace.putIfAbsent(v, k));

                for (String section : prefs.keys()) {
                    String namespace = sectionToNameSpace.getOrDefault(section, section);
                    for (String key : prefs.node(section).keys()) {
                        keys.add(namespace + namespaceSeparator + key);
                    }
                }
            } catch (BackingStoreException e) {
                throw new RuntimeException("Unexpected: " + e, e);
            }
        }
        // THEN ADD ALL KEYS IN PROVIDED COLLECTIONS
        if (!includeKeys.isEmpty()) {
            includeKeys.stream().map(Configurable::getConfProps)
                    .forEach(it -> keys.add(it.getFullyQualifiedName()));
        }
        return keys;

    }

    @Override
    public Set<String> keys(Configurable[] configurables, Configurable[]... moreConfigurables) {
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
        return keys(list);
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        keys(Collections.emptyList()).forEach(key -> {
            String value = stringFullyQualifiedName(key);
            if (value != null) {
                consumer.accept(key, value);
            }
        });
    }

    private String checkNode(String section, String key) {
        Preferences node = prefs.node(section);
        if (node != null) {
            return node.get(key, null);
        }
        return null;
    }

    private String getNamespaceKey(String namespace, String key) {
        String section = namespaceToINISection.getOrDefault(namespace, namespace);
        // FIRST CHECK - SEE IF SECTION HAS VALUE
        String value = checkNode(section, key);
        if (value != null || section.equals(namespace)) {
            return value;
        }
        // SECOND CHECK - SEE IF NAMESPACE HAS VALUE
        return checkNode(namespace, key);
    }


    public String getIniValue(Configurable configurable) {
        if (ini != null) {
            ConfProps confProps = configurable.getConfProps();
            return getNamespaceKey(confProps.getNameSpace(), confProps.getName());
        }
        return null;
    }


    public String getIniValueFullyQualifiedName(String nameSpaceSeperator, String fullyQualifiedName) {
        if (ini != null) {
            String namespace = StringUtils.substringBeforeLast(fullyQualifiedName, nameSpaceSeperator);
            String key = StringUtils.substringAfterLast(fullyQualifiedName, nameSpaceSeperator);
            return getNamespaceKey(namespace, key);
        }
        return null;
    }

    public String getIniValueFullyQualifiedName(String fullyQualifiedName) {
        return getIniValueFullyQualifiedName(namespaceSeparator, fullyQualifiedName);
    }

    ///////////////////////////////

    private String getConfValue(ConfProps confProps) {
        return getConfValue(confProps.getFullyQualifiedName());
    }

    // Programmatically set values override INI file values
    private String getConfValue(String fullyQualifiedName) {
        String value = conf.getOrDefault(fullyQualifiedName, null);
        return value == null ? getIniValueFullyQualifiedName(fullyQualifiedName) : value;
    }

    ////////////////////////////////

    @Override
    public String allSettings() {
        final StringBuilder settings = new StringBuilder();
        keys(Collections.emptyList())
                .forEach(k -> settings.append("key= '").append(k)
                        .append("'\t\t -> ")
                        .append("value= '").append(getConfValue(k)).append("'")
                        .append("\n"));
        return settings.toString();
    }

    @Override
    public final void put(Configurable configurable) {
        put(configurable, configurable.getConfProps().getDefaultValue());
    }


    @Override
    public final void put(final Configurable key, final String value) {
        putFullyQualifiedName(key.getConfProps().getFullyQualifiedName(), value);
    }


    @Override
    public void putFullyQualifiedName(final String fullyQualifiedName, final String value) {
        put(fullyQualifiedName, value, false);
    }

    @Override
    public boolean putIfNotNull(Configurable configurable) {
        return putIfNotNull(configurable, configurable.getConfProps().getDefaultValue());
    }

    @Override
    public boolean putIfNotNull(Configurable key, String value) {
        if (value != null) {
            put(key, value);
            return true;
        }
        return false;
    }

    /**
     * @param fullyQualifiedName the name of the key
     * @param value              the value to put
     * @param defaults           if null values should result in the removal of the existing entry
     */
    private void put(final String fullyQualifiedName, final String value, boolean defaults) {
        if (frozen) {
            throw new UnsupportedOperationException("This configuration has been frozen and is now immutable.");
        }
        if (value == null) {
            if (!defaults) {
                conf.remove(fullyQualifiedName);
                log.debug("value associated with '{}' has been removed", fullyQualifiedName);
            }
        } else {
            conf.put(fullyQualifiedName, value);
            log.debug("put '{}' -> '{}'", fullyQualifiedName, value);
        }
    }

    ////////////////////////
    // using Configurable //
    ////////////////////////

    @Override
    public String string(Configurable key) {
        return getConfValue(key.getConfProps());
    }

    @Override
    public float floating(Configurable key) {
        return Float.parseFloat(getConfValue(key.getConfProps()));
    }

    @Override
    public double doubling(Configurable key) {
        return Double.parseDouble(getConfValue(key.getConfProps()));
    }

    @Override
    public int integer(Configurable key) {
        return Integer.parseInt(getConfValue(key.getConfProps()));
    }

    @Override
    public boolean booling(Configurable key) {
        return Boolean.parseBoolean(getConfValue(key.getConfProps()));
    }

    ////////////////////////////////
    // using fully qualified name //
    ////////////////////////////////

    @Override
    public String stringFullyQualifiedName(String fullyQualifiedName) {
        return getConfValue(fullyQualifiedName);
    }

    @Override
    public float floatingFullyQualifiedName(String fullyQualifiedName) {
        return Float.parseFloat(getConfValue(fullyQualifiedName));
    }

    @Override
    public double doublingFullyQualifiedName(String fullyQualifiedName) {
        return Double.parseDouble(getConfValue(fullyQualifiedName));
    }

    @Override
    public int integerFullyQualifiedName(String fullyQualifiedName) {
        return Integer.parseInt(getConfValue(fullyQualifiedName));
    }

    @Override
    public boolean boolingFullyQualifiedName(String fullyQualifiedName) {
        return Boolean.parseBoolean(getConfValue(fullyQualifiedName));
    }
}
