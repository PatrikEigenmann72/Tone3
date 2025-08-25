// --------------------------------------------------------------------------------------------
// File: Samael/HuginAndMunin/Log.java
// This file is part of the Samael.HuginAndMunin library and provides a logging utility.
// It includes methods for writing log messages on the levels of errors and exceptions,
// warnings, and informational or verbose messages. With this utility, developers can
// persist application behavior to a log file for later inspection. The log messages are
// timestamped and categorized for clarity.
// --------------------------------------------------------------------------------------------
// Author:          Patrik Eigenmann
// eMail:           p.eigenmann72@gmail.com
// GitHub:          https://github.com/PatrikEigenmann72/HelloJWorld
// --------------------------------------------------------------------------------------------
// Change Log:
// Tue 2025-08-19 Initial Java implementation based on Debug.java.                  Version: 00.01
// Tue 2025-08-19 Replaced try-with-resources with explicit close().                Version: 00.02
// Sun 2025-08-24 Making sure that Log file is in the personal documents folder.    Version: 00.03
// --------------------------------------------------------------------------------------------
package samael.huginandmunin;

/**
 * Standard Java imports. These are the necessary imports for file I/O and date/time formatting.
 */
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is part of the samael.huginandmunin library. The Log class provides a logging utility
 * for writing log messages at different levels (error, warning, info, verbose) to a log file
 * (application name.log). The file will be stored in the user's Documents\Logs directory.
 */
public final class Log {

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

    /**
     * This enum defines the different log levels available in the logging utility.
     */
    public enum LogLevel {
        None(0),                                                            // No logging
        @SuppressWarnings("PointlessBitwiseExpression")
        Error(1 << 0),                                                      // Error logging
        Warning(1 << 1),                                                    // Warning logging
        Info(1 << 2),                                                       // Info logging
        Verbose(1 << 3),                                                    // Verbose logging
        All(Error.value | Warning.value | Info.value | Verbose.value);      // All logging

        /**
         * The bitmask value representing the log level.
         */
        public final int value;
        
        /**
         * Constructor for the LogLevel enum.
         */
        LogLevel(int value) { this.value = value; }
    }

    /**
     * The bitmask value representing the active log levels.
     */
    private static int bitmask = LogLevel.All.value;

    /**
     * The name of the log file.
     */
    private static String logFileName;

    /**
     * The init method initializes the logging utility with a specific log file name.
     */
    public static void init(String fileName) {
        String documentFolder = getDocumentsPath();
        File logDir = new File(documentFolder, "Logs");
        if (!logDir.exists() && !logDir.mkdirs()) {
            debug("Failed to create log directory: " + logDir.getAbsolutePath());
            return;
        }

        logFileName = new File(logDir, fileName).getAbsolutePath();

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(logFileName, false));
            // Overwrite file with empty start
        } catch (IOException ex) {
            debug("Failed to initialize log file: " + ex.getMessage());
        } finally {
            close(writer);
        }
    }

    /**
     * Sets the bitmask for the active log levels.
     * @param bitmaskIn The new bitmask value.
     */
    public static void setBitmask(int bitmaskIn) {
        bitmask = bitmaskIn;
    }

    /**
     * Writes a log message to the log file.
     * @param level The log level of the message.
     * @param message The log message.
     * @param component The name of the component logging the message.
     */
    public static void writeLine(LogLevel level, String message, String component) {
        if ((bitmask & level.value) == 0 || logFileName == null) return;

        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String line = String.format("%s [%s] [%s] %s", timestamp, level.name(), component, message);

        PrintWriter writer = null;
        try {
            writer = open();
            writer.println(line);
        } catch (IOException ex) {
            debug("Log write failed: " + ex.getMessage());
        } finally {
            close(writer);
        }
    }

    /**
     * Writes an exception stack trace to the log file.
     * @param ex The exception to log.
     */
    public static void writeException(Exception ex) {
        if ((bitmask & LogLevel.Error.value) == 0 || logFileName == null) return;

        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        PrintWriter writer = null;
        try {
            writer = open();
            writer.println(timestamp + " [Exception] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            for (StackTraceElement elem : ex.getStackTrace()) {
                writer.println("  at " + elem.toString());
            }
        } catch (IOException e) {
            debug("Exception log failed: " + e.getMessage());
        } finally {
            close(writer);
        }
    }

    /**
     * Opens the log file for writing.
     * @return A PrintWriter for the log file.
     * @throws IOException If an I/O error occurs.
     */
    private static PrintWriter open() throws IOException {
        return new PrintWriter(new FileWriter(logFileName, true), true);
    }

    /**
     * Closes the log file.
     * @param writer The PrintWriter for the log file.
     */
    private static void close(PrintWriter writer) {
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * Resolves the user's personal Documents folder path on Windows systems.
     * <p>
     * This method invokes PowerShell to query the actual location of the "MyDocuments" folder,
     * ensuring compatibility with redirected folders (e.g., moved to a secondary drive).
     * The result is trimmed and returned as a string. If the PowerShell invocation fails
     * or returns an empty result, the method falls back to the default profile-based path
     * using {@code user.home\Documents}.
     * <p>
     * This approach avoids external dependencies and ensures lean, platform-aware behavior.
     *
     * @return The absolute path to the user's Documents folder, respecting redirection if available.
     */
    public static String getDocumentsPath() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-Command",
                "[Environment]::GetFolderPath('MyDocuments')"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String path = reader.readLine();

                process.waitFor();  // Ensure process completes
                process.destroy();  // Explicitly destroy

                return path != null ? path.trim() : null;
            }
        } catch (IOException | InterruptedException e) {
            debug("Getting Document Path failed, fallback to " + System.getProperty("user.home") + "\\Documents");
            return System.getProperty("user.home") + "\\Documents"; // fallback
        }
    }

    /**
     * Logs debug information for configuration errors.
     * @param key The configuration key that caused the error.
     * @param value The configuration value that caused the error.
     * @param type The expected type of the configuration value.
     */
    private static void debug(String msg) {
        String timestamp = java.time.LocalTime.now()
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
            .toString(); // e.g. 20:32:56.286

        System.out.println(String.format(
            "%s%s [Error] [Log] %s%s",
            ANSI_RED,
            timestamp,
            msg,
            ANSI_RESET
        ));
    }
}