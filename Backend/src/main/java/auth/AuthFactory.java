package auth;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;

public class AuthFactory implements ConfigFactory {
	
	 private final String salt;

    public AuthFactory(final String salt) {
        this.salt = salt;
    }

	@Override
	public Config build() {
        
		final ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(salt));
        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());
        final HeaderClient headerClient = new HeaderClient(new JwtAuthenticator(salt));
        headerClient.setHeaderName("Authorization");
        headerClient.setPrefixHeader("Bearer ");
        
        parameterClient.setSupportGetRequest(true);
        
        Clients clients = new Clients(parameterClient, directBasicAuthClient, headerClient);
        
        final Config config = new Config(clients);
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        
        return config;
	}

}
