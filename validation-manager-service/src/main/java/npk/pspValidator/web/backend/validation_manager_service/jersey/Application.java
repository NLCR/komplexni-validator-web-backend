package npk.pspValidator.web.backend.validation_manager_service.jersey;

import npk.pspValidator.web.backend.utils.Config;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;

public class Application extends ResourceConfig {
    //TODO: unify if here or in web.xml
    public Application() throws IOException {
        System.out.println("initializing " + Application.class.getName());
        //register(CorsFilter.class);
        //register(LoggingFilter.class);
        Config.init();
    }
}
