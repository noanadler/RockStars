package auth;

import org.pac4j.core.context.WebContext;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.extractor.Extractor;

public class JsonFormExtractor implements Extractor<UsernamePasswordCredentials> {

    private final String usernameParameter;

    private final String passwordParameter;

    private final String clientName;

    public JsonFormExtractor(final String usernameParameter, final String passwordParameter, final String clientName) {
        this.usernameParameter = usernameParameter;
        this.passwordParameter = passwordParameter;
        this.clientName = clientName;
    }

    @Override
    public UsernamePasswordCredentials extract(WebContext context) {
        final String username = context.getRequestParameter(this.usernameParameter);
        final String password = context.getRequestParameter(this.passwordParameter);
        if (username == null || password == null) {
            return null;
        }

        return new UsernamePasswordCredentials(username, password, clientName);
    }

    public String getUsernameParameter() {
        return usernameParameter;
    }

    public String getPasswordParameter() {
        return passwordParameter;
    }
}
