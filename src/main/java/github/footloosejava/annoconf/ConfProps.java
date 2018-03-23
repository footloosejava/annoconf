package github.footloosejava.annoconf;


import java.lang.annotation.Annotation;

public interface ConfProps {

    String getName();

    String getDescription();

    String getDefaultValue();

    /**
     * It is recommended that classes of this return `getClass().getName()` in order to
     * provide the same namespace management.
     *
     * @return the namespace
     */
    String getNameSpace();

    /**
     * The Fully Qualified Name is usually the namespace + "-" and the getName() result.
     * A configurable does not need to have a NameSpace.
     * <p>
     * Maps that store configuration values should store the name as the FullyQualified name to prevent collisions
     * unless the key is a global simple name with no conflicts (ie. global namespace).
     *
     * @return the fully qualified name of this instance
     */
    String getFullyQualifiedName();

    Annotation getAnnotation();

    /**
     * @param configuration the configuration to look for the fully qualified name in
     * @return the value if it exists or default value or null.
     */
    String getValue(Configuration configuration);
}
