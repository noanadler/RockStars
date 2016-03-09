import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import data.HerokuDataSource;

public class AlertCrawler {
    public static void main(String[] args) {
		String format = "MMMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
    	
    	Sql2o sql2o = new Sql2o(new HerokuDataSource());
    	
    	Connection con = sql2o.open();
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect("http://wwwnc.cdc.gov/travel/destinations/list").get();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		HashMap<String, String> countryLinks = new HashMap<String, String>();
		Elements bullets = doc.getElementsByClass("list-bullet");
		for(Element bullet : bullets ){
			Elements CountryData = bullet.getElementsByTag("li");
			for(Element Country : CountryData){
				countryLinks.put( Country.getElementsByTag("a").text(),  Country.getElementsByTag("a").attr("href"));
			}				
		}
		
		Iterator it = countryLinks.entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry pair = (HashMap.Entry)it.next();
	        String country = pair.getKey().toString();
	        String link = pair.getValue().toString();
	        
	        doc = null;
			try {
				doc = Jsoup.connect("http://wwwnc.cdc.gov" + link).get();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
	    	Element notices = doc.getElementById("travel-notices");
	    	if(notices != null)
	    	{
		    	Elements alerts = notices.getElementsByTag("li");
		    	for (Element alert : alerts) {
		    		  String alertTitle = (alert.getElementsByTag("a")).text();
		    		  String alertSummary = (alert.getElementsByClass("summary")).text();
		    		  String alertStatus = "started_at";
		    		  String date = (alert.getElementsByClass("date")).text();
		    		  Date alertDate = null;
		    		  
		    		  try {
		    		      alertDate = sdf.parse(date);
		    		  } catch (ParseException e) {
		    			  System.out.println(e.getMessage());
		    		  }
		    		  
		    	      String insertTravelAlertSQL = "INSERT INTO alerts(country_id, title, description, started_at) " +
		    	    			"VALUES (:country_id, :title, :description, :started_at)";
		    		  
		    		  if(alertSummary.contains("has been removed.") || alertSummary.equals("Removed"))
		    		  {
		    			  alertStatus = "ended_at";
			    	      insertTravelAlertSQL = "INSERT INTO alerts(country_id, title, description, ended_at) " +
			    	    			"VALUES (:country_id, :title, :description, :ended_at)";
		    		  }		    		  
		    		  
		    		  try {
					    	con.createQuery(insertTravelAlertSQL)
					        .addParameter("country_id", country)
					        .addParameter("title", alertTitle)
					        .addParameter("description", alertSummary)
					        .addParameter(alertStatus, alertDate)
					        .executeUpdate();	
						} catch (Sql2oException e) {
							System.out.println(e.getMessage());
						}
		    		  
		    		}
	    	}
	        it.remove();
	    }
		
		
    	
    }
}