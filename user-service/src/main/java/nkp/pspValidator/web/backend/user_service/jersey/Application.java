package nkp.pspValidator.web.backend.user_service.jersey;

import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.jersey.CorsFilter;
import nkp.pspValidator.web.backend.utils.jersey.LastModifiedSanitizingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * see web.xml
 */
public class Application extends ResourceConfig {

    public Application() throws IOException {
        Logger.getLogger(Application.class.getName()).info("initializing");
        //no need to register it explicitly here, see web.xml, it works because the class is in jersey.config.server.provider.packages
        //register(CorsFilter.class);
        //register(LoggingFilter.class);
        //filtr z utils neni pokryty package scanningem (jersey.config.server.provider.packages), proto explicitni registrace
        register(LastModifiedSanitizingFilter.class);
        register(CorsFilter.class);
        Config.init();
    }
}

