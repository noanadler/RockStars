package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.sql2o.Sql2o;

import com.sendgrid.*;

import data.HerokuDataSource;
import models.Alert;
import models.Sql2oModel;
import models.User;

public class EmailGenerator {
	private static SendGrid _mailer;
	private static final String _newLineSeparator;
	private static final String GRID_USER = "app47892308@heroku.com";
	private static final String GRID_PASS = "apxwsmtc8054";
	private static final String ME = "doNotReply@SafteTravels.com";
	private static final String VERIFICATION_TITLE = "Welcome to SafeTravels";
	private static final String GOODBYE_TITLE = "We're sorry to see you go";
	private static final String UPDATE_TITLE = "Your daily report is here!";
	private Sql2oModel _model;
    
    public EmailGenerator(){
    	_model = new Sql2oModel(new Sql2o(new HerokuDataSource()));
    }
	
	static{
		_mailer = new SendGrid(GRID_USER,GRID_PASS);
		_newLineSeparator = System.getProperty("line.separator");
	}
	
	public void sendVerificationEmail(String userEmail, String userName, UUID uId){
		String emailText = generateVerificationText(userName, uId);
		generateAndSendEmail(userEmail, VERIFICATION_TITLE, emailText);	
    }
	public void sendNotificationsToUser(String userEmail, List<String> userCountries, 
			HashMap<String, ArrayList<Alert>> alertsByCountry){
    	User user = _model.getUserByEmail(userEmail);
    	if(!user.getNotifications()) return;
    	String emailText = generateUpdateString(user.getName(), user.getId(), userCountries, alertsByCountry);
    	generateAndSendEmail(userEmail, UPDATE_TITLE, emailText);
    }
    public void sendGoodbyeEmail(String userEmail, String userName){
    	String emailText = generateGoodbyeText(userName);
    	generateAndSendEmail(userEmail, GOODBYE_TITLE, emailText);
    }
	private void generateAndSendEmail(String to, String subject, String emailText){
    	try{
    		SendGrid.Email email = new SendGrid.Email();
    		email.addTo(to);
    		email.setFrom(ME);
    		email.setSubject(subject);
    		email.setText(emailText);
    		SendGrid.Response res =_mailer.send(email);
    		System.out.println(res);
    	}
		catch(SendGridException ex){
			System.out.println(ex);
		}
    }
	private String generateUpdateString(String userName, UUID uId, 
			List<String> userCountries, HashMap<String, ArrayList<Alert>> alertsByCountry){
		String template1 = "Hey, %s!";
		String template2 = "Here's everything you should be aware of today to better protect your health:";
		StringBuilder builder = new StringBuilder();
		for(String country : userCountries){
			List<Alert> alerts = alertsByCountry.get(country);
			if(alerts == null || alerts.isEmpty()) continue;
			builder.append(country + ":" + _newLineSeparator);
			builder.append("----------"+ _newLineSeparator + _newLineSeparator);
			for(Alert alert : alerts){
				builder.append("Alert title: " + alert.getTitle() + _newLineSeparator);
				builder.append("Alert summary: " + alert.getDescription() + _newLineSeparator);
				builder.append("Alert date: " + new DateTime(alert.getStarted_at()) + _newLineSeparator);
				builder.append(_newLineSeparator + _newLineSeparator);
			}
		}
		String template3 = builder.toString();
		String template4 = "That's all for today. If you are no longer insterested in"
				+ " getting these emails, please unregister by clicking the following link:";
		String template5 = "https://safe-travels.herokuapp.com/noNotifications?userId=%s";
		String template6 = "Stay healthy and happy,";
    	String template7 = "The SafeTravels team";
    	return String.format(template1, userName) + _newLineSeparator + _newLineSeparator + template2 + 
    			_newLineSeparator + _newLineSeparator + template3 + template4 + _newLineSeparator + 
    			String.format(template5, uId) + _newLineSeparator + _newLineSeparator + template6 +
    			_newLineSeparator + _newLineSeparator + template7;			
	}
    private String generateVerificationText(String userName, UUID uId) {
    	String template1 = "Welcome to SafeTravels, %s!";
    	String template2 = "You are receiving this email following your request to register "
    			+ "with SafeTravels. It is our pleasure to help you take good care of yourself while "
    			+ "traveling.";
    	String template3 = "To complete the registration process, please click the following link: ";
    	String template4 = "https://safe-travels.herokuapp.com/register?userId=%s";
    	String template5 = "Stay healthy and happy,";
    	String template6 = "The SafeTravels team";
    	return String.format(template1, userName) + _newLineSeparator + _newLineSeparator + template2 + _newLineSeparator + template3 +
    			String.format(template4, uId) + _newLineSeparator + _newLineSeparator + template5 + 
    			_newLineSeparator + _newLineSeparator + template6;
	}
    private String generateGoodbyeText(String userName) {
    	String template1 = "Hi %s!";
    	String template2 = "We realize you may no longer require our services. Sometimes you "
    			+ "just have to say goodbye. If you are ever interested in working with us again, "
    			+ "you know where to find us.";
    	String template3 = "Stay healthy and happy,";
    	String template4 = "The SafeTravels team";
		return String.format(template1, userName)+ _newLineSeparator + _newLineSeparator + template2 + _newLineSeparator +
				_newLineSeparator + template3 + _newLineSeparator + _newLineSeparator + template4;
	}
}
