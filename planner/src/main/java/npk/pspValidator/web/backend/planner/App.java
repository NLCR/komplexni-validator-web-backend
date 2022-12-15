/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package npk.pspValidator.web.backend.planner;

import npk.pspValidator.web.backend.utils.Config;

import java.io.File;
import java.io.IOException;

public class App {
    public String getGreeting() {
        return "Hello from Planner.";
    }

    public static void main(String[] args) throws IOException {
        System.out.println(new App().getGreeting());

        Config.init();
        Planner planner = new Planner();
        System.out.println(planner.run());
    }
}
