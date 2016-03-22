package auth;

import org.pac4j.core.config.Config;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;

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
	
	@Override
    public void handle(Request request, Response response) {
		if(!request.requestMethod().equals("OPTIONS")) {
			super.handle(request,  response);
		}
    }
}
