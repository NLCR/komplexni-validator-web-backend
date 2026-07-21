package nkp.pspValidator.web.backend.utils.jersey;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS filtr spolecny pro vsechny sluzby.
 * Misto wildcard '*' echuje konkretni Origin z requestu - kombinace
 * 'Access-Control-Allow-Origin: *' + 'Access-Control-Allow-Credentials: true' je prohlizecem zakazana,
 * jakmile frontend posle request s credentials (withCredentials/credentials: 'include').
 * Neni auto-registrovany pres jersey.config.server.provider.packages (lezi v utils),
 * kazda sluzba ho musi registrovat ve sve tride Application.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {
    //TODO: pripadne omezit na povolene originy z konfigurace

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || origin.isEmpty()) {
            //neni CORS request (curl, komunikace mezi sluzbami apod.)
            return;
        }
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.putSingle("Access-Control-Allow-Origin", origin);
        //odpoved se lisi podle Originu, aby cache (proxy) nevracela cizi Access-Control-Allow-Origin
        headers.add("Vary", "Origin");
        headers.putSingle("Access-Control-Allow-Credentials", "true");
        headers.putSingle("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
