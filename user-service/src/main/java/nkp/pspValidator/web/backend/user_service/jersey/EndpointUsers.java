package nkp.pspValidator.web.backend.user_service.jersey;

import nkp.pspValidator.web.backend.user_service.db.User;
import nkp.pspValidator.web.backend.user_service.db.UserDatabase;
import nkp.pspValidator.web.backend.user_service.db.UserDatabaseImpl;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import nkp.pspValidator.web.backend.utils.auth.OauthHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class EndpointUsers {

    private final OauthHelper oauthHelper = new OauthHelper();
    private final AuthHelper authHelper = new AuthHelper();
    private UserDatabase userDatabase = new UserDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("Authorization") String authorizationHeader) {
        try {
            //admin, system
            authHelper.authenticateAndExtractSystemOrAdminUserId(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            JSONArray result = new JSONArray();
            for (User user : userDatabase.getUsers()) {
                result.put(userToJson(user));
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
    public Response getUserById(@HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") String userId) {
        nkp.pspValidator.web.backend.utils.dao.User thisUser;
        try {
            //admin, system, owner
            thisUser = authHelper.authenticateAndExtractUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            User user = userDatabase.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (thisUser.admin || thisUser.id.equals(user.id)) {
                return Response.ok(userToJson(user).toString()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("error", "user is not admin and is not owner of requested user")).build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PUT
    @Path("/authenticated")
    //@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthenticatedUserFetchingDbData(@HeaderParam("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("error", "missing authorization header")).build();
            }
            if (!authorizationHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("error", "unsupported authorization method")).build();
            }
            String jwtStr = authorizationHeader.substring("Bearer ".length());
            JSONObject jwt = oauthHelper.decodeJwt(jwtStr);
            boolean verified = oauthHelper.verifyGoogleIdToken(jwtStr);
            if (!verified) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("error", "OAUTH verification failed")).build();
            }
            JSONObject userData = jwt.getJSONObject("payload");
            //System.out.println(userData.toString(2));
            String userId = userData.getString("email");
            User userFromDb = userDatabase.getUserById(userId);
            if (userFromDb == null) {
                //System.out.println("user not found in DB");
                User user = new User(userId, userData.getString("email"), userData.getString("picture"), userData.getString("given_name"), userData.getString("family_name"), userData.getString("name"), false, false, null, null);
                userDatabase.insertUser(user);
                return Response.ok(userToJson(user).toString()).build();
            } else {
                //System.out.println("user found in DB");
                return Response.ok(userToJson(userFromDb).toString()).build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PUT
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("Authorization") String authorizationHeader, @PathParam("userId") String userId, String folderDefinitionStr) {
        try {
            //admin only
            authHelper.authenticateAndExtractAdminUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            User user = userDatabase.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            JSONObject definition = new JSONObject(folderDefinitionStr);
            if (definition.has("admin")) user.admin = definition.getBoolean("admin");
            if (definition.has("verified")) user.verified = definition.getBoolean("verified");
            userDatabase.updateUser(user);
            return Response.ok(userToJson(user).toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private JSONObject userToJson(User user) {
        JSONObject result = new JSONObject();
        result.put("id", user.id);
        result.put("email", user.email);
        result.put("pictureUrl", user.pictureUrl);
        result.put("givenName", user.givenName);
        result.put("familyName", user.familyName);
        result.put("name", user.name);
        result.put("verified", user.verified);
        result.put("admin", user.admin);
        result.put("institutionName", user.institutionName);
        result.put("institutionSigla", user.institutionSigla);
        return result;
    }
}
