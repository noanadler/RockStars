import static spark.Spark.*;


import models.Country;
import models.Model;
import models.Sql2oModel;

import org.json.JSONObject;
import org.sql2o.Sql2o;

import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.JsonNode;

import data.ArrayConverter;
import data.HerokuDataSource;

public class Main {
    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        Quirks arraySupport = ArrayConverter.arrayConvertingQuirks(new PostgresQuirks(), true, false);
    	Sql2o sql2o = new Sql2o(new HerokuDataSource(), arraySupport);
        Model model = new Sql2oModel(sql2o);
        
        get("/test", (req, res) -> {
        	HttpResponse<JsonNode> response = Unirest.get("http://polaris.i3l.gatech.edu:8080/gt-fhir-webapp/base/Observation/1?_format=json").asJson();
        	JSONObject obj = response.getBody().getObject();
        	return obj.toString();
        });
        
        get("/country/:name", (req, res) -> {
        	Country country = model.getCountry(req.params("name"));
			Gson gson = new Gson();
			String json = gson.toJson(country); 
			
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