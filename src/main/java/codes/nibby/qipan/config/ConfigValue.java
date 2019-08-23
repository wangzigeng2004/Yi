package codes.nibby.qipan.config;

/**
 * The values in this enum corresponds to the unique $ values in .json config files.
 * These will be handled differently by the app when loading the respective setting.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public enum ConfigValue {

    /*
        Example in config.json:
        "theme": {
            "use": "megumi"
            "stones": "$AS_DEFINED"
        }

        Meaning:
            Use stone textures as defined in the "megumi" theme template.
     */
    /** The value is as specified by some other template. */
    AS_DEFINED,

    /**
     * As a substitute for image based assets such as stone texture.
     * The application will use its own drawing methods for displaying them.
     *
     * On some config values this is unsupported and may throw an exception.
     */
    APP_RENDERED,


    /*
        Example in config.json:
        "theme": {
            "use": "megumi"
            "stones": "convex"
        }

        Meaning:
            convex is not one of the values in the enum, therefore it is a
            customisation which will override the theme template.
     */
    /** The default value if it's not one of the other reserved prefix values. */
    OVERRIDE;

    /**
     * Parses a given config value string into one of the enum values.
     * If no matching names are found, OVERRIDE is returned by default.
     *
     * @param value Config string value
     * @return An enum from this enum set.
     */
    public static ConfigValue parse(String value) {
        value = value.replace("$", "");
        for (ConfigValue v : ConfigValue.values()) {
            if (value.toUpperCase().equals(v.name()))
                return v;
        }
        return OVERRIDE;
    }
}
