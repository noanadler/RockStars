package auth;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

import spark.Request;
import spark.Response;

public class XHRRequiresAuthenticationFilter extends RequiresAuthenticationFilter {
    public XHRRequiresAuthenticationFilter(final Config config, final String clientName) {
    	super(config, clientName);
    }

    public XHRRequiresAuthenticationFilter(final Config config, final String clientName, final String authorizerName) {
    	super(config, clientName, authorizerName);
    }

    public XHRRequiresAuthenticationFilter(final Config config, final String clientName, final String authorizerName, final String matcherName) {
    	super(config, clientName, authorizerName, matcherName);

    }	
	
	@SuppressWarnings("rawtypes")
	@Override
    public void handle(Request request, Response response) {
		if(!request.requestMethod().equals("OPTIONS")) {
			 CommonHelper.assertNotNull("config", config);
		        final MySparkWebContext context = new MySparkWebContext(request, response, config.getSessionStore());
		        CommonHelper.assertNotNull("config.httpActionAdapter", config.getHttpActionAdapter());
		        UserProfile profile = null;
		        String header = context.getRequestHeader("Authorization");
		        if(header != null && header.contains("Bearer "))
		        {
		        	String tokenString = header.replaceAll("Bearer ", "");
			        try {
				        final JWT jwt = JWTParser.parse(tokenString);
			            final JWEObject jweObject = (JWEObject) jwt;
			            CommonHelper.assertNotBlank("encryptionSecret", AuthenticationHelpers.JWT_SALT);

		                jweObject.decrypt(new DirectDecrypter(AuthenticationHelpers.JWT_SALT.getBytes("UTF-8")));
		                // Extract payload
		                SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
		                JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
		                System.out.println(claimSet);
				        String subject = claimSet.getSubject();
				        //if subject is  a valid user
				        profile = new UserProfile();
				        profile.setId(subject.substring(12));
				        profile.addAttribute("email", subject.substring(12));
			            System.out.println(profile);
					} catch (ParseException | JOSEException | UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		        		        
		        
		        
		        logger.debug("url: {}", context.getFullRequestURL());
		        logger.debug("matcherName: {}", matcherName);
		        if (matchingChecker.matches(context, this.matcherName, config.getMatchers())) {
		            final Clients configClients = config.getClients();
		            CommonHelper.assertNotNull("configClients", configClients);
		            logger.debug("clientName: {}", clientName);
		            final List<Client> currentClients = clientFinder.find(configClients, context, this.clientName);
		            logger.debug("currentClients: {}", currentClients);

		            logger.debug("profile: {}", profile);

		            if (profile != null) {
		                logger.debug("authorizerName: {}", authorizerName);
		                if (authorizationChecker.isAuthorized(context, profile, authorizerName, config.getAuthorizers())) {
		                    logger.debug("authenticated and authorized -> grant access");
		                } else {
		                    logger.debug("forbidden");
		                    forbidden(context, currentClients, profile);
		                }
		            } else {
		                if (startAuthentication(context, currentClients)) {
		                    logger.debug("Starting authentication");
		                    saveRequestedUrl(context, currentClients);
		                    redirectToIdentityProvider(context, currentClients);
		                } else {
		                    logger.debug("unauthorized");
		                    unauthorized(context, currentClients);

		                }
		            }

		        } else {

		            logger.debug("no matching for this request -> grant access");
		        }
		}
    }
}
