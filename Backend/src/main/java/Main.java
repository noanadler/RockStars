import static spark.Spark.*;

import java.sql.DriverManager;

import org.json.JSONObject;
import org.postgresql.ds.PGSimpleDataSource;
import org.sql2o.Connection;
import org.sql2o.Sql2o;






import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.JsonNode;

import data.HerokuDataSource;

public class Main {
    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        get("/test", (req, res) -> {
        	HttpResponse<JsonNode> response = Unirest.get("http://polaris.i3l.gatech.edu:8080/gt-fhir-webapp/base/Observation/1?_format=json").asJson();
        	JSONObject obj = response.getBody().getObject();
        	return obj.toString();
        });
        
        get("/test_db", (req, res) -> {
        	Sql2o sql2o = new Sql2o(new HerokuDataSource());
        	
        	try(Connection con = sql2o.open()) {
        		System.out.println("CONNCECTED");
        		return "CONNECTED";
        	} catch (Exception e) {
        		return e.getMessage();
        	}
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