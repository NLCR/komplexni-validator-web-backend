package npk.pspValidator.web.backend.results_service.jersey;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LoggingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // TODO: custom logging
        System.out.println("LoggingFilter.filter()");
        System.out.printf("%s %s\n", requestContext.getMethod(), requestContext.getUriInfo().getPath());
    }
}
