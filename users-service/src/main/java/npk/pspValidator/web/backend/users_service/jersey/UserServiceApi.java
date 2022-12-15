package npk.pspValidator.web.backend.users_service.jersey;

import npk.pspValidator.web.backend.users_service.db.OauthHelper;
import npk.pspValidator.web.backend.users_service.db.User;
import npk.pspValidator.web.backend.users_service.db.UserDatabase;
import npk.pspValidator.web.backend.users_service.db.UserDatabaseImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserServiceApi {

    private final OauthHelper oauthHelper = new OauthHelper();

    private UserDatabase userDatabase = new UserDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        try {
            JSONArray result = new JSONArray();
            for (User user : userDatabase.getUsers()) {
                result.put(user.toJson());
            }
            return Response.ok(result.toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("userId") String userId) {
        try {
            User user = userDatabase.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(user.toJson().toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PUT
    @Path("/authenticated")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthenticatedUserFetchingDbData(String idToken) {
        try {
            //TODO: pouzivat session tokeny a ne vzdy overovat idToken
            JSONObject jwt = oauthHelper.decodeJwt(idToken);
            boolean verified = oauthHelper.verifyGoogleIdToken(idToken);
            if (!verified) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("OAUTH verification failed").build();
            }
            JSONObject userData = jwt.getJSONObject("payload");
            //System.out.println(userData.toString(2));
            String userId = userData.getString("email");
            User userFromDb = userDatabase.getUserById(userId);
            if (userFromDb == null) {
                //System.out.println("user not found in DB");
                User user = new User(userId, userData.getString("email"), userData.getString("picture"), userData.getString("given_name"), userData.getString("family_name"), userData.getString("name"), false, false, null, null);
                userDatabase.insertUser(user);
                return Response.ok(user.toJson().toString()).build();
            } else {
                System.out.println("user found in DB");
                return Response.ok(userFromDb.toJson().toString()).build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
