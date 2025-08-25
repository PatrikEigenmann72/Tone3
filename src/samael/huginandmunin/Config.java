/* ------------------------------------------------------------------------------------------------
 * Config.java -  The Config class is a singleton class, so it is only one time instantiated
 * within the application. I decided to create an in memory key=value storage for security
 * reasons. Config files can have sensitive data like passwords and API keys. If these sensitive
 * data are stored in plain text, they could be easily accessed by unauthorized users. By using
 * an in-memory storage solution, we can minimize the risk of exposing sensitive information.
 * Still handle with care.
 * ------------------------------------------------------------------------------------------------
 * Author:  Patrik Eigemann
 * eMail:   p.eigenmann72@gmail.com
 * GitHub:  www.github.com/PatrikEigemann72/HelloJWorld
 * ------------------------------------------------------------------------------------------------
 * Change Log:
 * Mon 2025-08-18 File created.                                                     Version: 00.01
 * Mon 2025-08-18 Refactored to use Lazy Holder Pattern.                            Version: 00.02
 * Mon 2025-08-18 Added static proxy methods for get/set.                           Version: 00.03
 * Mon 2025-08-18 Renamed instance methods to avoid shadowing.                      Version: 00.04
 * Mon 2025-08-18 Updated constructor to use setSetting with namespaced keys.       Version: 00.05
 * Thu 2025-08-21 Added support for int configuration values.                       Version: 00.06
 * Thu 2025-08-21 Added a lean debug method to avoid dependencies to other classes. Version: 00.07
 * Thu 2025-08-21 Added support for boolean configuration values.                   Version: 00.08
 * Thu 2025-08-21 Added support for float configuration values.                     Version: 00.09
 * Thu 2025-08-21 Added support for double configuration values.                    Version: 00.10
 * Thu 2025-08-21 Added support for char configuration values.                      Version: 00.11
 * Fri 2025-08-22 Added support for String configuration values.                    Version: 00.12
 * Fri 2025-08-22 Private scope for get method.                                     Version: 00.13
 * ------------------------------------------------------------------------------------------------ */
package samael.huginandmunin;

// Standard Java imports. These imports are needed to have the Config class working properly.
import java.util.HashMap;
import java.util.Map;

/**
 * The Config class is a lazy initialized thread safe singleton class, so it is only one time
 * instantiated within the application. I decided to create an in memory key=value storage for
 * security reasons. Config files can have sensitive data like passwords and API keys. If these
 * sensitive data are stored in plain text, they could be easily accessed by unauthorized users.
 * By using an in-memory storage solution, we can minimize the risk of exposing sensitive
 * information. Still handle this class with care.
 */
public final class Config {

    /**
     * ANSI escape code for red text. Needed in the in-class debug section.
     * I decided to use in-class debug messages to avoid dependencies on other classes.
     */
    private static final String ANSI_RED = "\u001B[31m";

    /**
     * ANSI escape code for resetting text color. Needed in the in-class debug section.
     * I decided to use in-class debug messages to avoid dependencies on other classes.
     */
    private static final String ANSI_RESET = "\u001B[0m";

    /** Inner static class responsible for holding the singleton instance. */
    private static class Holder {
        private static final Config INSTANCE = new Config();
    }

    /** Returns the singleton instance of Config. */
    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    /** In-memory key=value store */
    private final Map<String, String> settings;

    /** Private constructor to prevent external instantiation */
    private Config() {
        settings = new HashMap<>();
        setSetting("App.Name", "HelloJWorld");
        setSetting("App.Version", "00.03");
        setSetting("App.Author", "Patrik Eigemann");
        setSetting("App.Label.Text", "Hello Java World!");
        setSetting("App.Label.Font", "Courier New");
        setSetting("App.LogName", "HelloJWorld.log");
        setSetting("App.Width", "300");
        setSetting("App.Height", "120");
    }

    /**
     * Static proxy for getting a config value
     * @param key The configuration key to retrieve.
     * @return The configuration value as a string, or null if not found.
     */
    private static String get(String key) {
        return getInstance().getSetting(key);
    }

    /**
     * Retrieves a configuration value as a string.
     * @param key The configuration key to retrieve.
     * @return The configuration value as a string, or an empty string if not found.
     */
    public static String getString(String key) {
        String value = get(key);
        if (value == null) {
            debug(key, "null", "String");
            return "";
        }
        return value;
    }

    /**
     * Retrieves a configuration value as an integer.
     * @param key The configuration key to retrieve.
     * @return The configuration value as an integer, or 0 if not found or invalid.
     */
    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            debug(key, value, "int");
            return 0;
        }
    }

    /**
     * Retrieves a configuration value as a boolean.
     * Accepts "true" or "false" (case-insensitive). Returns false if invalid or missing.
     * 
     * @param key The configuration key to retrieve.
     * @return The configuration value as a boolean, or false if not found or invalid.
     */
    public static boolean getBoolean(String key) {
        String value = get(key);
        if (value == null) {
            debug(key, "null", "boolean");
            return false;
        }

        String normalized = value.trim().toLowerCase();

        if (normalized.equals("true") || normalized.equals("yes") || normalized.equals("on") ||
            normalized.equals("enable") || normalized.equals("enabled") || normalized.equals("ja") ||
            normalized.equals("y") || normalized.equals("t") || normalized.equals("j") ||
            normalized.matches("^[1-9]$")) {
            return true;
        }

        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("off") ||
            normalized.equals("disable") || normalized.equals("disabled") || normalized.equals("nein") ||
            normalized.equals("0") || normalized.equals("f") || normalized.equals("n") ||
            normalized.matches("^-\\d+$")) {
            return false;
        }

        debug(key, value, "boolean");
        return false;
    }

    /**
     * Retrieves a configuration value as a float.
     * Returns 0.0f if invalid or missing.
     * @param key The configuration key to retrieve.
     * @return The configuration value as a float, or 0.0f if not found or invalid.
     */
    public static float getFloat(String key) {
        String value = get(key);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            debug(key, value, "float");
            return 0.0f;
        }
    }

    /**
     * Retrieves a configuration value as a double.
     * Returns 0.0 if invalid or missing.
     * @param key The configuration key to retrieve.
     * @return The configuration value as a double, or 0.0 if not found or invalid.
     */
    public static double getDouble(String key) {
        String value = get(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            debug(key, value, "double");
            return 0.0;
        }
    }

    /**
     * Retrieves a configuration value as a char.
     * Returns '\0' if missing or if the string is empty.
     * @param key The configuration key to retrieve.
     * @return The configuration value as a char, or '\0' if not found or invalid.
     */
    public static char getChar(String key) {
        String value = get(key);
        if (value == null || value.isEmpty()) {
            debug(key, value, "char");
            return '\0';
        }
        return value.charAt(0);
    }

    /**
     * Instance method for getting a config value (not intended for direct use)
     * @param key The configuration key to retrieve.
     * @return The configuration value as a string, or null if not found.
     */
    private String getSetting(String key) {
        return settings.get(key);
    }

    /**
     * Instance method for setting a config value (not intended for direct use)
     * @param key The configuration key to set.
     * @param value The configuration value to set.
     */
    private void setSetting(String key, String value) {
        settings.put(key, value);
    }

    /**
     * Logs debug information for configuration errors.
     * @param key The configuration key that caused the error.
     * @param value The configuration value that caused the error.
     * @param type The expected type of the configuration value.
     */
    private static void debug(String key, String value, String type) {
        String timestamp = java.time.LocalTime.now()
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
            .toString(); // e.g. 20:32:56.286

        System.out.println(String.format(
            "%s%s [Error] [Config] Key='%s' failed to convert string '%s' into %s.%s",
            ANSI_RED,
            timestamp,
            key,
            value != null ? value : "null",
            type,
            ANSI_RESET
        ));
    }
}