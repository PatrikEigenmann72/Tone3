// ---------------------------------------------------------------------------------------------------
// File: Samael/HuginAndMunin/Log.java
// This file is part of the Samael.HuginAndMunin library and provides a logging utility. It includes
// methods for writing log messages on the levels of errors and exceptions, warnings, and informational
// or verbose messages. With this utility, developers can persist application behavior to a log file
// for later inspection. The log messages are timestamped and categorized for clarity.
// ---------------------------------------------------------------------------------------------------
// Author:  Patrik Eigenmann
// eMail:   p.eigenmann72@gmail.com
// GitHub:  https://github.com/PatrikEigenmann72/HelloJWorld
// ---------------------------------------------------------------------------------------------------
// Change Log:
// Tue 2025-08-19 Initial Java implementation based on Debug.java.                      Version: 00.01
// Tue 2025-08-19 Replaced try-with-resources with explicit close().                    Version: 00.02
// Wed 2025-08-20 Bitmask for log levels and enum LogLevel added.                       Version: 00.03
// Wed 2025-08-20 Added writeException() to log stack traces of exceptions.             Version: 00.04
// Thu 2025-08-21 Refactored to use PrintWriter for writing to log file.                Version: 00.05
// Sun 2025-08-24 Making sure that Log file is in the personal documents folder.        Version: 00.06
// Sun 2025-08-31 Added getDocumentsPath() to resolve OS specific paths.                Version: 00.07
// ---------------------------------------------------------------------------------------------------
package samael.huginandmunin;

/**
 * These are the imports we need—mostly for file handling and working with dates.
 * Nothing fancy, just the usual suspects from the standard Java library.
 * If we ever add external dependencies, they’ll show up here too—but for now, it’s all native.
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
     * These are the log levels we use to categorize messages—everything from quiet diagnostics to
     * loud failures. Each level helps us decide what gets printed, stored, or ignored, depending
     * on how noisy we want the system to be. If you’re adding a new level, make sure it plays well
     * with the filtering logic downstream.
     */
    public enum LogLevel {
        None(0),                                                      // No logging
        @SuppressWarnings("PointlessBitwiseExpression")                     // Suppress warning for All level definition
        Error(1 << 0),                                                      // Error logging
        Warning(1 << 1),                                                    // Warning logging
        Info(1 << 2),                                                       // Info logging
        Verbose(1 << 3),                                                    // Verbose logging
        All(Error.value | Warning.value | Info.value | Verbose.value);      // All logging

        /**
         * This value holds the bitmask for the current log level.
         * It lets us flip specific logging flags on or off without dragging around extra state.
         * If you're tweaking levels or adding new ones, make sure the masks stay mutually exclusive.
         */
        public final int value;
        
        /**
         * This is the constructor for the LogLevel enum. Each level—Error, Warning, Info, or
         * Verbose—is declared with a leading capital and lowercase remainder. The constructor
         * assigns a bitmask to each level, allowing us to combine them using bitwise operations
         * for flexible filtering and output control.
         */
        LogLevel(int value) { this.value = value; }
    }

    /**
     * Holds the bitmask for whichever log levels are currently active. Combines levels like
     * Error | Info or Warning | Verbose using bitwise flags, so the logger knows exactly what
     * to print—no extra logic, no redundant state, just clean filtering.
     */
    private static int bitmask = LogLevel.All.value;

    /**
     * Name of the log file where messages get written. Could be absolute or relative,
     * depending on how the logger is configured—just make sure it points somewhere writable.
     */
    private static String logFileName;

    /**
     * Initializes the logging utility with the specified log file name. This sets the output
     * destination for all log entries—whether it's a relative path, absolute path, or something
     * dynamically generated. The file needs to be writable at runtime, since every log level
     * funnels through this unless redirected elsewhere.
     * 
     * @param fileName The name of the log file (e.g., "application.log").
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
     * Sets the bitmask that defines which log levels are currently active.
     * Combines levels like Error | Info or Warning | Verbose using bitwise flags,
     * allowing the logger to filter output without extra logic or state.
     *
     * @param bitmaskIn the new bitmask value representing active log levels
     */
    public static void setBitmask(int bitmaskIn) {
        bitmask = bitmaskIn;
    }

    /**
     * Writes a log entry to the active log file. The message is tagged with its log level
     * and the name of the component that generated it, so downstream readers or tools can
     * filter, trace, or analyze output more effectively.
     *
     * @param level the severity or category of the message (e.g., Error, Info, Verbose)
     * @param message the actual content to be logged
     * @param component the logical source of the message, useful for tracing system behavior
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
     * Logs the full stack trace of the given exception to the active log file. Useful for
     * diagnosing unexpected failures, especially when paired with contextual log messages.
     *
     * @param ex the exception to capture and write to the log output
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
     * Opens the log file and prepares it for writing. If the file doesn’t exist, it’ll be created;
     * if it does, new log entries will be appended unless configured otherwise. This method assumes
     * the file path is valid and writable—any I/O issues will bubble up.
     *
     * @return a PrintWriter tied to the log file, ready to receive output
     * @throws IOException if the file can’t be accessed, created, or written to
     */
    private static PrintWriter open() throws IOException {
        return new PrintWriter(new FileWriter(logFileName, true), true);
    }

    /**
     * Closes the log file stream and releases any system resources tied to it.
     * Should be called when logging is complete to avoid file locks or memory leaks.
     *
     * @param writer the PrintWriter instance currently writing to the log file
     */
    private static void close(PrintWriter writer) {
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * Resolves the path to the user's Documents folder in a platform-aware way.
     * <p>
     * On Windows, runs a PowerShell command to query the actual location of "MyDocuments",
     * which accounts for folder redirection (e.g., moved to another drive or network share).
     * If the query fails or returns nothing, falls back to {@code user.home\Documents}.
     * <p>
     * On non-Windows systems, defaults directly to {@code user.home/Documents}.
     * This method avoids external dependencies and keeps behavior predictable across platforms.
     *
     * @return the absolute path to the user's Documents folder, honoring redirection on Windows
     */
    public static String getDocumentsPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
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
                    process.waitFor();
                    process.destroy();

                    if (path != null && !path.trim().isEmpty()) {
                        return path.trim();
                    }
            }
        } catch (IOException | InterruptedException e) {
                debug("PowerShell failed, falling back to user.home/Documents");
            }
            return System.getProperty("user.home") + File.separator + "Documents";
        }

        // macOS, Linux, etc.
        return System.getProperty("user.home") + File.separator + "Documents";
    }

    /**
     * Logs debug output related to configuration errors. Useful for tracing setup issues,
     * unexpected values, or fallback behavior during initialization.
     *
     * @param msg the message to be displayed in the debug log
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