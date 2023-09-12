package nkp.pspValidator.web.backend.job_execution_service.jersey;

import nkp.pspValidator.web.backend.utils.Config;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * see web.xml
 */
public class Application extends ResourceConfig {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    public Application() throws IOException {
        logger.info("initializing");
        //no need to register it explicitly here, see web.xml, it works because the class is in jersey.config.server.provider.packages
        //register(CorsFilter.class);
        //register(LoggingFilter.class);
        Config.init();
    }

}