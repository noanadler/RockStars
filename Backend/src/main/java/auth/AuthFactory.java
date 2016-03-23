package auth;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
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
		MyProfileCreator profileCreator = new MyProfileCreator();
		final HeaderClient headerClient = new HeaderClient(new JwtAuthenticator(salt), profileCreator);
        headerClient.setHeaderName("Authorization");
        headerClient.setPrefixHeader("Bearer ");
        
        Clients clients = new Clients(headerClient);
        
        final Config config = new Config(clients);
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        
        return config;
	}
	
	public static byte[] hashPassword( final char[] password, final byte[] salt ) {		 
	       try {
	           SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
	           PBEKeySpec spec = new PBEKeySpec( password, salt, 1000, 16 );
	           SecretKey key = skf.generateSecret( spec );
	           byte[] res = key.getEncoded( );
	           return res;
	 
	       } catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
	           throw new RuntimeException( e );
	       }
	   }

}
