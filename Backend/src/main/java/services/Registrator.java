package services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import data.HerokuDataSource;
import models.Sql2oModel;
import models.User;
import models.Country;
import org.sql2o.Sql2o;

public class Registrator {

    private static final String ERR_USER_EXISTS = "This email address is already in our system.";
    private Sql2oModel _model;
    
    public Registrator(){
    	_model = new Sql2oModel(new Sql2o(new HerokuDataSource()));
    }
    public void beginRegistration(String userEmail, String userName, String userGender, StringBuilder errorReporter){
        if(emailExists(userEmail)){
            errorReporter.append(ERR_USER_EXISTS);
            return;
        }
        UUID uId = UUID.randomUUID();
        addUser(uId, userEmail,userName, userGender);
        EmailGenerator gen = new EmailGenerator();
        gen.sendVerificationEmail(userEmail, uId);
    }
    public boolean uIdExists(UUID uId){
    	return _model.getUserByUid(uId) == null;
    }
    public boolean isSubscribedToNotifications(UUID uId){
    	User user = _model.getUserByUid(uId);
    	return user.getNotifications();
    }
    public void verifyUser(UUID uId){
    	User user = _model.getUserByUid(uId);
    	user.setVerified(true);
    	_model.updateUser(user);
    }
    public boolean emailExists(String userEmail){
    	return _model.getUserByEmail(userEmail) == null;
    }
    public void addUser(UUID uId, String userName, String userEmail, String userGender){
        _model.insertUser(uId, userName, userEmail, userGender, new Country[0]);
    }
    public void updateNotificationCountries(String userEmail, String[] selectedCountries){
    	List<String> toAdd = new ArrayList<>();
    	List<String> toRem = new ArrayList<>();
    	User user = _model.getUserByEmail(userEmail);
    	getCountriesIntersect(user.getCountries(), selectedCountries, toRem);
    	getCountriesIntersect(selectedCountries, user.getCountries(), toAdd);
    	for(String r : toRem){
    		removeSubscriber(r, userEmail);
    	}
    	for(String a : toAdd){
    		addSubscriber(a, userEmail);
    	}
    	user.setCountries(selectedCountries);
    	_model.updateUser(user);
    }
    public List<String> getCountrySubscribers(String country){
    	return _model.getCountrySubscribers(country);
    }
    public void setNotificationsStatus(UUID uId, boolean enable){
    	User user = _model.getUserByUid(uId);
    	user.setNotifications(enable);
    	_model.updateUser(user);
    }
    public void deregisterFromNotifications(UUID uId, boolean userRequested){
    	User user = _model.getUserByUid(uId);
    	String userEmail = user.getEmail();
    	String userName = user.getName();
    	for(String country : user.getCountries()){
    		removeSubscriber(country, userEmail);	
    	}
    	user.setNotifications(false);
    	_model.updateUser(user);
    	if(userRequested){
    		EmailGenerator gen = new EmailGenerator();
    		gen.sendGoodbyeEmail(userEmail, userName);
    	}
    }
    private void removeSubscriber(String country, String userEmail){
    	List<String> subs = _model.getCountrySubscribers(country);
    	if(subs == null) return;
    	HashSet<String> subsSet = new HashSet<>(subs);
    	subsSet.remove(userEmail);
    	_model.updateSubscribers(country, new ArrayList<String>(subsSet));
    }
    private void addSubscriber(String country, String userEmail){
    	List<String> subs = _model.getCountrySubscribers(country);
    	if(subs == null){
    		subs = new ArrayList<String>();
    		
    	}
    	subs.add(userEmail);
    	_model.updateSubscribers(country, subs);
    }
    private void getCountriesIntersect(String[] l1, String[] l2, List<String> res){
    	for(String c1 : l1){
    		boolean found = false;
    		for(String c2 : l2){
    			if(c1.equals(c2)){
    				found = true;
    				break;
    			}
    		}
    		if(!found){
    			res.add(c1);
    		}
    	}
    }
}