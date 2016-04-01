package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.sql2o.Sql2o;
import data.HerokuDataSource;
import models.Alert;
import models.Sql2oModel;

public class Notifier {
	
	private Sql2oModel _model;
    
    public Notifier(){
    	_model = new Sql2oModel(new Sql2o(new HerokuDataSource()));
    }
	public boolean sendDailyUpdates(){
		HashMap<String, ArrayList<Alert>> alertsByCountry;
		alertsByCountry = AlertCrawler.crawl();
		if(alertsByCountry == null){
			return false;
		}
		List<String> alertCountries = _model.getDistinctAlertCountries();
		HashMap<String,List<String>> userNotifications = new HashMap<>();
		for(String country : alertCountries){
			List<String> countrySubscribers = _model.getCountrySubscribers(country);
			for(String subscriber : countrySubscribers){
				List<String> userCountries = userNotifications.get(subscriber);
				if(userCountries == null){
					userCountries = new LinkedList<String>();
				}
				userCountries.add(country);
				userNotifications.put(subscriber, userCountries);
			}
		}
		EmailGenerator gen = new EmailGenerator();
		for(String userEmail : userNotifications.keySet()){
			gen.sendNotificationsToUser(userEmail, userNotifications.get(userEmail), alertsByCountry);
		}
		return true;
	}
	
}