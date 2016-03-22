package auth;

import org.pac4j.core.context.session.SessionStore;
import org.pac4j.sparkjava.SparkWebContext;

import spark.Request;
import spark.Response;

public class MySparkWebContext extends SparkWebContext {
	private final Request request;

	public MySparkWebContext(Request request, Response response, SessionStore sessionStore) {
		super(request, response, sessionStore);
		// TODO Auto-generated constructor stub
		this.request = request;
	}
	
	public String getRequestBody() {
		return request.body();
	}

}
