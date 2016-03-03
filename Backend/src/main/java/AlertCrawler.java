import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AlertCrawler {
    public static void main(String[] args) {
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect("http://wwwnc.cdc.gov/travel/destinations/list").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String, String> countryLinks = new HashMap<String, String>();
		Elements bullets = doc.getElementsByClass("list-bullet");
		for(Element bullet : bullets ){
			Elements CountryData = bullet.getElementsByTag("li");
			for(Element Country : CountryData){
				String countryName = Country.getElementsByTag("a").text();
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
				e.printStackTrace();
			}
	    	Element notices = doc.getElementById("travel-notices");
	    	if(notices != null)
	    	{
		    	Elements alerts = notices.getElementsByTag("li");
		    	for (Element alert : alerts) {
		    		  String alertTitle = (alert.getElementsByTag("a")).text();
		    		  String alertDate = (alert.getElementsByClass("date")).text();
		    		  String alertSummary = (alert.getElementsByClass("summary")).text();
		    		  if(!alertSummary.contains("has been removed.") && 
		    				  !alertSummary.equals("Removed"))
		    		  {
		    			  //This is an active alert ; we will need to replace these printouts with database upserts
			    		  System.out.println(country);
			    		  System.out.println(alertTitle);
			    		  System.out.println(alertSummary);
			    		  System.out.println(alertDate);
		    		  }
		    		  else
		    		  {
		    			  //We will need to remove this entry in the database
		    			  System.out.println("Removing " + alertTitle + " from " + country);
		    		  }
		    		}
	    	}
	        it.remove();
	    }
		
		
    	
    }
}