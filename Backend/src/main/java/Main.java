import static spark.Spark.*;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Country;
import models.Model;
import models.Sql2oModel;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import org.json.JSONObject;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;
import org.pac4j.sparkjava.SparkWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import auth.AuthFactory;
import auth.MySparkWebContext;
import auth.MyUserProfile;

import com.mashape.unirest.http.JsonNode;

import data.ArrayConverter;
import data.HerokuDataSource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

public class Main {
	
	private static UserProfile getUserProfile(final Request request, final Response response, final SessionStore sessionStore) {
		final MySparkWebContext context = new MySparkWebContext(request, response, sessionStore);
		
    	Gson gson = new Gson();
    	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
    	boolean existsUser = false;
    	/* need code here to look up user in db */
    	if(existsUser)
    	{
    		final ProfileManager manager = new ProfileManager(context);
    		return manager.get(true);
    	}else
    	{
    		Map<String, Object> attributes = new HashMap<String, Object>();
    		attributes.put("attribute", "true");
    		MyUserProfile myProfile = new MyUserProfile(loginParams.username, attributes);	
    		return myProfile;
    	}

	}
	
	private final static String JWT_SALT = "12341234123412341234123412341234";
    
    public static void main(String[] args) {
	    try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, java.lang.Boolean.FALSE);
	    } catch (Exception ex) {
	    }
        port(getHerokuAssignedPort());
        Quirks arraySupport = ArrayConverter.arrayConvertingQuirks(new PostgresQuirks(), true, false);
    	Sql2o sql2o = new Sql2o(new HerokuDataSource(), arraySupport);
        Model model = new Sql2oModel(sql2o);
               
        // JWT user auth setup
        final Config config = new AuthFactory(JWT_SALT).build();

        before("/testauth", new RequiresAuthenticationFilter(config, "HeaderClient"));
        
        /**
         * Sign up a user
         */
        post("/signup", (req, res) -> {
        	// store user info into database
        	
        	// pass to authenticator to get web token
        	
        	// return token
        	return "";
        });
        
        /**
         * Login a user
         */
        //accept basic auth info in HTTPS header, return token
        post("/login/", (req, res) -> {      
        	System.out.println("Session: " + config.getSessionStore());
        	final UserProfile profile = getUserProfile(req, res, config.getSessionStore());
        	System.out.println("Profile: " + profile);
    		JwtGenerator generator = new JwtGenerator(JWT_SALT);
    		String token = "";
    		if (profile != null) {
    			token = generator.generate(profile);
    		}
    		final Map map = new HashMap();
    		map.put("token", token);
    		
    		Gson gson = new Gson();
			String json = gson.toJson(map); 
			
			res.status(200);
			res.type("application/json");
        	return json;
    		
        });
        
        //this protected page is only visible with a valid JWT token
        get("/testauth", (req, res) -> {          
        	List<Country> countries = model.getCountries();
			Gson gson = new Gson();
			String json = gson.toJson(countries); 
			
			res.status(200);
			res.type("application/json");
        	return json;
    		
        });
        
        get("/country/:name", (req, res) -> {
        	Country country = model.getCountry(req.params("name"));
			Gson gson = new Gson();
			String json = gson.toJson(country); 
			
			res.status(200);
			res.type("application/json");
        	return json;
        }); 
        
        get("/countries", (req, res) -> {
        	List<Country> countries = model.getCountries();
			Gson gson = new Gson();
			String json = gson.toJson(countries); 
			
			res.status(200);
			res.type("application/json");
        	return json;
        }); 
        
        // SETUP CORS
        options("/*", (request, response) -> {
    		String accessControlRequestHeaders = request
    				.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
            	response.header("Access-Control-Allow-Headers",
            			accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request
            		.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
            	response.header("Access-Control-Allow-Methods",
        			accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
        	response.header("Access-Control-Allow-Origin", "*");
        });        
    }
    
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}

class AuthRequest {
	public String username;
	public String password;
	AuthRequest() {}
}
