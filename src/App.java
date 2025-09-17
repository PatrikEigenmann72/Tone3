/* ---------------------------------------------------------------------------------------
 * App.java -  The App class serves as the entry point for the application. When the user
 * launches the program—whether by double-clicking an icon or running it from the command
 * line—the system needs a defined starting point. This class provides that by containing
 * the main method.
 *
 * The main method is where execution begins, initializing necessary components and setting
 * the application into motion. It ensures that the first frame, user interface, and core
 * functionality are properly instantiated.
 *
 * In a structured software environment, every project needs a single place where the
 * system knows to start. This class fulfills that role, keeping the application streamlined
 * and predictable across multiple use cases.
 * ---------------------------------------------------------------------------------------
 * Author:  Patrik Eigemann
 * eMail:   p.eigenmann@gmx.net
 * GitHub:  www.github.com/PatrikEigemann/Java
 * ---------------------------------------------------------------------------------------
 * Change Log:
 * Mon 2025-05-26 File created.                                             Version: 00.01
 * Sun 2025-08-24 Import samael.huginandmunin.*                             Version: 00.02
 * Wed 2025-09-03 Improvement: Started App with mode 3 sine / pink noise.   Version: 00.03
 * Thu 2025-09-04 Enhancement: Switching between subs and tops implemented. Version: 00.04
 * Wed 2025-09-17 Enhancement: Added method loadIcon.                       Version: 00.05
 * Wed 2025-09-17 Enhancement: Load icon from resources.                    Version: 00.06
 * ---------------------------------------------------------------------------------------
 * ToDo List:
 * - I need a radio buttons to switch between Subwoofer testing and Mains testing. This
 *   will require for subwoofer testing that the frequency range of the slider is from
 *   20hz to 200hz, while for mains testing it should be from 200hz to 20khz. This new
 *   feature will need to have a startup sequence and a configuration.              Done.
 * -------------------------------------------------------------------------------------- */
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;
import samael.huginandmunin.*;
import tone3.gui.*;

/**
 * App.java -  The App class serves as the entry point for the application. When
 * the user launches the program—whether by double-clicking an icon or running it
 * from the command line—the system needs a defined starting point. This class
 * provides that by containing the main method.
 *
 * The main method is where execution begins, initializing necessary components and
 * setting the application into motion. It ensures that the first frame, user
 * interface, and core functionality are properly instantiated.
 *
 * In a structured software environment, every project needs a single place where
 * the system knows to start. This class fulfills that role, keeping the application
 * streamlined and predictable across multiple use cases.
 */
public class App {

    /**
     * The main method acts as the starting point of the application. When the program
     * is launched, execution begins here, ensuring that all necessary components are
     * initialized and the user interface is displayed.
     *
     * This method is responsible for setting the application in motion. It ensures
     * that the primary frame is created and rendered within the correct thread
     * context to maintain responsiveness and stability.
     *
     * In structured software design, having a clearly defined entry point allows for
     * predictable execution, making the application easy to manage and extend.
     *
     * @param args Command-line arguments passed during application startup.
     *             These can be used for configuration or debugging but are
     *             typically not required in standard executions.
     */
    public static void main(String[] args) {

        Debug.init(args);
        Log.init(Config.getString("App.LogName"));

        Debug.setBitmask(Debug.DebugLevel.All.value);
        Log.setBitmask(Log.LogLevel.All.value);

        Debug.writeLine(Debug.DebugLevel.Info, "Application is starting...", "App");
        Log.writeLine(Log.LogLevel.Info, "Application is starting...", "App");

        // Using SwingUtilities.invokeLater to start a
        // Swing application as background task.
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();

            // Icon logic added here.
            String msg;
            String icon = "resources/icons/" + Config.getString("App.IconName");
            Image appIcon = loadIcon(icon);
            if (appIcon != null) {
                msg = "Application icon loaded from resources: " + icon;
                Debug.writeLine(Debug.DebugLevel.Info, msg, "App");
                Log.writeLine(Log.LogLevel.Info, msg, "App");
                mf.setIconImage(appIcon);
            } else {
                msg = "Application icon missing: " + icon;
                Debug.writeLine(Debug.DebugLevel.Warning, msg, "App");
                Log.writeLine(Log.LogLevel.Warning, msg, "App");
            }

            mf.setVisible(true);
        });
    }

    /**
     * Loads an image resource from the application's classpath.
     * <p>
     * This method is designed to retrieve a specific icon file—typically located under
     * {@code resources/icons/}—and return it as an {@link java.awt.Image} object.
     * It uses the class loader to access resources packaged inside the JAR.
     * <p>
     * All diagnostic output is routed through {@code Debug} and {@code Log}, ensuring
     * consistent logging and traceability. If the resource is missing or unreadable,
     * the method returns {@code null} and logs the appropriate messages.
     * <p>
     * This method is intended for use during application startup to set the window icon.
     * Future versions may be modularized into {@code samael.tabernacle.ResourceLoader}.
     *
     * @param icon the full classpath-relative path to the icon file (e.g. {@code resources/icons/sinewave.png})
     * @return the loaded {@code Image} object, or {@code null} if loading fails
     */
    public static Image loadIcon(String icon) {
        String msg;

        try (InputStream stream = App.class.getClassLoader().getResourceAsStream(icon)) {
            if (stream == null) {
                msg = "Missing resource: " + icon;
                Debug.writeLine(Debug.DebugLevel.Warning, msg, "App");
                Log.writeLine(Log.LogLevel.Warning, msg, "App");
                return null;
            }
            return ImageIO.read(stream);
        } catch (Exception e) {
            msg = "Failed to load icon: " + icon;
            Debug.writeLine(Debug.DebugLevel.Error, msg, "App");
            Log.writeLine(Log.LogLevel.Error, msg, "App");
            Debug.writeException(e);
            Log.writeException(e);
            return null;
        }
    }
}