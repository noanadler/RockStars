package auth;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;

public class AuthFactory implements ConfigFactory {
	
	 private final String salt;

    public AuthFactory(final String salt) {
        this.salt = salt;
    }

	@Override
	public Config build() {
		final HeaderClient headerClient = new HeaderClient(new MyJwtAuthenticator(salt, salt));
        headerClient.setHeaderName("Authorization");
        headerClient.setPrefixHeader("Bearer ");
        
        Clients clients = new Clients(headerClient);
        
        final Config config = new Config(clients);
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        
        return config;
	}
}
