import static spark.Spark.*;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import models.Country;
import models.Model;
import models.Sql2oModel;
import models.User;

import services.Registrator;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.CommonHelper;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;



import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;


import auth.AuthFactory;
import auth.AuthRequest;
import auth.AuthenticationHelpers;
import auth.MySparkWebContext;
import auth.XHRRequiresAuthenticationFilter;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import data.ArrayConverter;
import data.HerokuDataSource;

public class Main {

    
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
        Registrator registrator = new Registrator();
        
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://52.72.172.54:8080/fhir/baseDstu2";        
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
	               
        // JWT user auth setup
        final Config config = new AuthFactory(AuthenticationHelpers.JWT_SALT).build();

        before((request, response) -> {
        	response.header("Access-Control-Allow-Origin", "*");
        });  
        
        before("/testauth", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        before("/users", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        before("/users/:uuid", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        before("/currentuuid", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        before("/vaccine/add", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));

        
        /**
         * Sign up a user
         */
        post("/signup", (req, res) -> {
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore());  		
        	Gson gson = new Gson();
        	System.out.println(context.getRequestBody());
        	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
        	//hash password
        	String hashedPassword = AuthenticationHelpers.hashPassword(loginParams.password.toCharArray());
        	// store user info into database
        	UUID uuid = registrator.beginRegistration(loginParams.email, hashedPassword, loginParams.name,  loginParams.gender, new StringBuilder());
        	//Ensure user has not already signed up
        	if(uuid == null)
        	{
        		res.status(409);
    			res.type("application/json");
            	return "This email address is already registered";
        	}
        	//create FHIR patient record
            User user = model.getUserByUid(uuid);
            user.createFHIRPatientRecord(client);
            model.updateFhirId(user);
        	// pass to authenticator to get web token
        	String token = AuthenticationHelpers.getUserToken(AuthenticationHelpers.createUserProfile(loginParams));
        	// return token
        	res.status(200);
			res.type("application/json");
        	return token; 
        });
        
        /**
         * Login a user
         */
        //accept basic auth info in HTTPS header, return token
        post("/login/", (req, res) -> {  
        	//1. Check username and password
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore()); 
        	Gson gson = new Gson();
        	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
        	//Ensure user has not already signed up
        	User user = model.getUserByEmail(loginParams.email);
        	if(user == null)
        	{
        		res.status(403);
    			res.type("application/json");
            	return "No such user";
        	}
        	if(!AuthenticationHelpers.checkPassword(loginParams.password, user.getHashedPassword()))
        	{
        		res.status(401);
    			res.type("application/json");
            	return "Invalid password";
        	}
        	//2. Generate user profile
        	String token = AuthenticationHelpers.getUserToken(AuthenticationHelpers.createUserProfile(loginParams));
        	//3. Return token
			res.status(200);
			res.type("application/json");
        	return token;  		
        });
        
        /**
         * Creates a new User and saves him in the database
         */
        //accept basic auth info in HTTPS header, return token
        put("/users/:uuid", (req, res) -> {  
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore()); 
        	Gson gson = new Gson();
        	UUID uuid = UUID.fromString(req.params("uuid"));
        	User user = model.getUserByUid(uuid);
        	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
        	if(user == null)
        	{
        		res.status(403);
    			res.type("application/json");
            	return "No such user";
        	}

        	if(loginParams.name != null){user.setName(loginParams.name);}
        	if(loginParams.email != null){user.setEmail(loginParams.email);}
        	if(loginParams.password != null){user.setHashedPassword(AuthenticationHelpers.hashPassword(loginParams.password.toCharArray()));}
        	if(loginParams.gender != null){user.setGender(loginParams.gender);}
        	if(loginParams.countries != null){user.setCountries(loginParams.countries);}
        	if(loginParams.notification != null){user.setNotifications(Boolean.valueOf(loginParams.notification));}
        	model.updateUser(user);
        	String json = gson.toJson(user);
        	//3. Return token
			res.status(200);
			res.type("application/json");
        	return json;  		
        });
        
        // Return JSON list of users
        get("/users", (req, res) -> {
        	Gson gson = new Gson();
        	List<User> users = model.getUsers();
        	String json = gson.toJson(users);
			
			res.status(200);
			res.type("application/json");
        	return json;        	
        });
        
     // Return JSON list of users
        get("/currentuuid", (req, res) -> {
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore());
        	String header = context.getRequestHeader("Authorization").replaceAll("Bearer ", "");
        	String userEmail = "";
		        try {
			        final JWT jwt = JWTParser.parse(header);
		            final JWEObject jweObject = (JWEObject) jwt;
		            CommonHelper.assertNotBlank("encryptionSecret", AuthenticationHelpers.JWT_SALT);

	                jweObject.decrypt(new DirectDecrypter(AuthenticationHelpers.JWT_SALT.getBytes("UTF-8")));
	                // Extract payload
	                SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
	                JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
			        userEmail = claimSet.getSubject().replaceAll("UserProfile#", "");
	                System.out.println(userEmail);
				} catch (ParseException | JOSEException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
        	Gson gson = new Gson();
        	User user = model.getUserByEmail(userEmail);
        	String json = gson.toJson(user.getId());
			
			res.status(200);
			res.type("application/json");
        	return json;        	
        });
        
     // Return user by ID
        get("/users/:uuid", (req, res) -> { 
        	Gson gson = new Gson();
        	UUID uuid = UUID.fromString(req.params("uuid"));
        	User user = model.getUserByUid(uuid);
        	user.setHashedPassword(""); // we don't want to send hashed down to client
        	String json = gson.toJson(user);
			
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
        
        get("/register/:uuid", (req, res) -> {
        	UUID uuid = UUID.fromString(req.params("uuid"));
        	new Registrator().verifyUser(uuid);
			res.status(200);
			res.type("application/json");
        	return "{ \"success\": true }";
        });
        
        get("/noNotifications/:uuid", (req, res) -> {
        	UUID uuid = UUID.fromString(req.params("uuid"));
        	new Registrator().deregisterFromNotifications(uuid, true);
			res.status(200);
			res.type("application/json");
        	return "{ \"success\": true }";
        }); 
        
        
        post("/vaccine/add", (req, res) -> {
        	String vaccineName = req.queryParams("vaccine");
        	String vaccinatedDate = req.queryParams("vaccinatedDate");
        	UUID uuid = UUID.fromString(req.queryParams("vaccinatedUser"));
        	User user = model.getUserByUid(uuid);
        	DateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        	Date date = format.parse(vaccinatedDate);
        	try{
	        	model.addVaccineToUser(vaccineName, uuid, date);
	        	user.createFHIRImmunizationRecord(client, model.getVaccineByName(vaccineName), date);
        	}catch (Exception e){
        		res.status(500);
    			res.type("application/json");
            	return "{ \"success\": false }";
        	}
        	
        	res.status(200);
			res.type("application/json");
        	return "{ \"success\": true }";
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
    }
    
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
