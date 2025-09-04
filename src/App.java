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
 * ---------------------------------------------------------------------------------------
 * ToDo List:
 * - I need a radio buttons to switch between Subwoofer testing and Mains testing. This
 *   will require for subwoofer testing that the frequency range of the slider is from
 *   20hz to 200hz, while for mains testing it should be from 200hz to 20khz. This new
//   feature will need to have a startup sequence and a configuration.              Done.
 * -------------------------------------------------------------------------------------- */
import javax.swing.SwingUtilities;
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
//@Version(namespace = "HelloWorld", component = "App", major = 0, minor = 1)
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
        Log.setBitmask(Log.LogLevel.Info.value | Log.LogLevel.Error.value | Log.LogLevel.Warning.value);

        Debug.writeLine(Debug.DebugLevel.Info, "Application is starting...", "App");
        Log.writeLine(Log.LogLevel.Info, "Application is starting...", "App");

        // Using SwingUtilities.invokeLater to start a
        // Swing application as background task.
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}