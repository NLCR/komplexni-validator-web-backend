package nkp.pspValidator.web.backend.upload_service.jersey;

import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

@Path("/")
public class EndpointRoot {

    String serviceName = "upload-service";
    String version;
    String build;

    public EndpointRoot() {
        try (InputStream input = EndpointRoot.class.getClassLoader().getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            this.version = properties.getProperty("app.version");
            this.build = properties.getProperty("build.timestamp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");

        sb.append("<head>");
        sb.append("<title>").append(serviceName).append("</title>");
        sb.append("</head>");

        sb.append("<body>");

        sb.append("<div>");
        sb.append("<h1>").append("API for the ").append("<i>").append(serviceName).append("</i>").append("</h1>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<h2>Service info</h2>");
        sb.append("<p>").append("version: ").append(version).append("</p>");
        sb.append("<p>").append("build: ").append(build).append("</p>");
        sb.append("<p>").append("uptime: ").append(Config.instanceOf().getUptimeFormatted()).append("</p>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<h3>Relevant endpoints of this service</h3>");
        //sb.append("<p>").append("GET ").append("<a href=\"./api/results\">/api/results</a>").append("</p>");
        sb.append("<div>");

        sb.append("<div>");
        sb.append("<h2>Endpoints for all the services</h2>");
        sb.append("<div>");
        JSONObject servicesUrls = Config.instanceOf().getServicesUrls();
        for (String serviceName : servicesUrls.keySet().stream().sorted().collect(Collectors.toList())) {
            String serviceUrl = servicesUrls.getString(serviceName);
            sb.append("<p>").append(serviceName).append(": ").append("<a href=\"" + serviceUrl + "\">" + serviceUrl + "</a>").append("</p>");
        }
        sb.append("</div>");

        sb.append("</body>");
        sb.append("</html>");
        return Response.ok(sb.toString()).build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getText() {
        String result = "service-name: " + serviceName + "\n" +
                "version: " + version + "\n" +
                "build: " + build + "\n" +
                "services: " + Config.instanceOf().getServicesUrls().toString(1);
        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson() {
        try {
            JSONObject result = new JSONObject();
            result.put("service-name", serviceName);
            result.put("version", version);
            result.put("build", build);
            result.put("services", Config.instanceOf().getServicesUrls());
            return Response.ok(result.toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
