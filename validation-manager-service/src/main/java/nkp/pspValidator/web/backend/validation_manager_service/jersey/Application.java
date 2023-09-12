package nkp.pspValidator.web.backend.validation_manager_service.jersey;

import nkp.pspValidator.web.backend.planner.Scheduler;
import nkp.pspValidator.web.backend.utils.Config;
import org.glassfish.jersey.server.ResourceConfig;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * see web.xml
 */
public class Application extends ResourceConfig {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    private ScheduledExecutorService executorService;

    public Application() throws IOException {
        logger.info("initializing");
        //no need to register it explicitly here, see web.xml, it works because the class is in jersey.config.server.provider.packages
        //register(CorsFilter.class);
        //register(LoggingFilter.class);
        Config.init();

        //init planner
        boolean schedulePlanner = true;
        if (schedulePlanner) {
            executorService = Executors.newScheduledThreadPool(1);
            Runnable job = new ScheduledJobRunnable();
            //initial delay because Schedulers uses validations-manager api, which might not be ready yet
            executorService.scheduleAtFixedRate(job, 10, 5, TimeUnit.SECONDS); // Run every 5 seconds
            logger.info("Planner scheduled");
        }
    }


    @PreDestroy
    public void destroy() {
        logger.info("destroying");
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public class ScheduledJobRunnable implements Runnable {

        @Override
        public void run() {
            //System.out.println("running Scheduler!");
            try {
                Config.init();
                Scheduler scheduler = new Scheduler();
                scheduler.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /*try {
                Thread.sleep(new Random().nextInt(5000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
            //System.out.println("Scheduler finished!");
        }
    }
}
