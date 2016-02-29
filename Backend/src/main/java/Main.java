import static spark.Spark.*;


import org.json.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.JsonNode;

public class Main {
    public static void main(String[] args) {
        get("/test", (req, res) -> {
        	HttpResponse<JsonNode> response = Unirest.get("http://polaris.i3l.gatech.edu:8080/gt-fhir-webapp/base/Observation/1?_format=json").asJson();
        	JSONObject obj = response.getBody().getObject();
        	return obj.toString();
        });
    }
}