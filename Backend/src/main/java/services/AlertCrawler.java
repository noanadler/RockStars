package services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import models.Model;
import models.Alert;
import models.Sql2oModel;

public class AlertCrawler {
	
    @SuppressWarnings("rawtypes")
	public static HashMap<String, ArrayList<Alert>> crawl() {
		String format = "MMMM dd, yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
    	
    	Sql2o sql2o = new Sql2o(new HerokuDataSource());
    	
    	Connection con = sql2o.open();
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect("http://wwwnc.cdc.gov/travel/destinations/list").get();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		HashMap<String, String> countryLinks = new HashMap<String, String>();
		Elements bullets = doc.getElementsByClass("list-bullet");
		for(Element bullet : bullets ){
			Elements CountryData = bullet.getElementsByTag("li");
			for(Element Country : CountryData){
				String[] urlParts = Country.getElementsByTag("a").attr("href").split("/");		    	
		    	String countryKey = urlParts[urlParts.length - 1]; // ID
				countryLinks.put( countryKey,  Country.getElementsByTag("a").attr("href"));
			}				
		}
		Model model = new Sql2oModel(sql2o);
		HashMap<String, ArrayList<Alert>> curAlerts = new HashMap<>();
		model.clearAlerts();
		Iterator it = countryLinks.entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry pair = (HashMap.Entry)it.next();
	        String country = pair.getKey().toString();
	        String link = pair.getValue().toString();
	        ArrayList<Alert> countryAlerts = new ArrayList<>();
	        
	        doc = null;
			try {
				doc = Jsoup.connect("http://wwwnc.cdc.gov" + link).get();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return null;
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
		    			  return null;
		    		  }
		    		  if(alertSummary.contains("has been removed.") || alertSummary.equals("Removed")){
		    			  continue;
		    		  }
		    		  String insertTravelAlertSQL = "INSERT INTO alerts(country_id, title, description, started_at) " +
		    	    			"VALUES (:country_id, :title, :description, :started_at)";
		    		  
		    		  try {
					    	con.createQuery(insertTravelAlertSQL)
					        .addParameter("country_id", country)
					        .addParameter("title", alertTitle)
					        .addParameter("description", alertSummary)
					        .addParameter(alertStatus, alertDate)
					        .executeUpdate();	
						} catch (Sql2oException e) {
							System.out.println(e.getMessage());
							return null;
						}
		    		  Alert alertObj = new Alert();
		    		  alertObj.setTitle(alertTitle);
		    		  alertObj.setDescription(alertSummary);
		    		  alertObj.setStarted_at(alertDate);
		    		  countryAlerts.add(alertObj);
		    		}
	    	}
	    	curAlerts.put(country, countryAlerts);
	        it.remove();
	    }
		return curAlerts;
    }
}