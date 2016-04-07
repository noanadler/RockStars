package services;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;

import javax.print.attribute.standard.PrinterLocation;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;

import ca.uhn.fhir.model.dstu2.resource.TestScript.Setup;
import data.ArrayConverter;
import data.HerokuDataSource;
import models.Sql2oModel;
import models.User;

public class RegistratorTest {
	private Sql2oModel _model;
	private Registrator registrator;
	private String userName;
	private String userPassword;
	private String userEmail;
	private String userGender = "F";
	private String[] countries = {"Brazil"};	
	private Boolean verified = true;
	private Boolean notification = true;
	private UUID uId;
	private User user;
			

	@Before
	public void Setup() throws Exception {
		registrator = new Registrator();
    	Quirks arraySupport = ArrayConverter.arrayConvertingQuirks(new PostgresQuirks(), true, false);
       	_model = new Sql2oModel(new Sql2o(new HerokuDataSource(), arraySupport));
       	// Generating unique username, password and email
		long millis=System.currentTimeMillis();
		String datetime=new Date().toString();
        datetime=datetime.replace(" ", "");
        datetime=datetime.replace(":", "");
        datetime=datetime.replace("GMT", "");
        String rndchars=RandomStringUtils.randomAlphanumeric(16);
        String str = rndchars + datetime + millis;
        userName = "test_name_" + str;
        userPassword = "test_password_" + str;
        userEmail = "test_email_" + str + "@gmail.com";
        uId = registrator.addUser(userName, userPassword, userEmail, userGender, countries, verified, notification);
        user = _model.getUserByUid(uId);
	}
	
	@After
	public void tearDown() throws Exception {
		registrator = null;
	}
	
	@Test
	public void testBeginRegistration() {

	}
	
	/* 
	 * The method uIdExists in the Registrator class 
	 * returns true if the user does not exist and false if the user exists.
	 * Shoudln't it be the opposite? 
	 */
	@Test
	public void testUidExists1() {
        assertEquals(true, registrator.uIdExists(uId));
	}
	
	/*
	 * This test fails since the method models.Sql2oModel.getUserByUid 
	 * cannot deal with a uId that does not exists
	 */
	@Test
	public void testUidExists2() {
		UUID falseUID = UUID.randomUUID();
		assertEquals(false, registrator.uIdExists(falseUID));
	}
	
	@Test
	public void testIsSubscribedToNotifications1(){
		user.setNotifications(true);
		_model.updateUser(user);
		assertEquals(true, registrator.isSubscribedToNotifications(uId));
    }
	
	@Test
	public void testIsSubscribedToNotifications2(){
		user.setNotifications(false);
		_model.updateUser(user);
		assertEquals(false, registrator.isSubscribedToNotifications(uId));
    }
	
	@Test
    public void testVerifyUser(){
    	user.setVerified(false);
    	_model.updateUser(user);
    	registrator.verifyUser(uId);
    	user =  _model.getUserByUid(uId);
    	assertEquals(true, user.getVerified());
    }
	
	@Test
    public void testEmailExists1(){
		assertEquals(true, registrator.emailExists(userEmail));
    }
	
	@Test
    public void testEmailExists2(){
		long millis=System.currentTimeMillis();
		String datetime=new Date().toString();
        datetime=datetime.replace(" ", "");
        datetime=datetime.replace(":", "");
        datetime=datetime.replace("GMT", "");
        String rndchars=RandomStringUtils.randomAlphanumeric(16);
        String str = rndchars + datetime + millis;
        userEmail = "test_email_" + str + "@gmail.com";
		assertEquals(false, registrator.emailExists(userEmail));
    }
	
	/*
	 * The call to registrator.addUser is in the @Before method.
	 * Here I only test that the method worked properly 
	 */
	@Test
    public void testAddUser(){
		assertEquals(userName, user.getName());
		assertEquals(userPassword, user.getHashedPassword());
		assertEquals(userEmail, user.getEmail());
		assertEquals(userGender, user.getGender());
		assertArrayEquals(countries, user.getCountries());
		assertEquals(verified, user.getVerified());
		assertEquals(notification, user.getNotifications());
    }
	
	@Test
    public void testUpdateNotificationCountries(){
    	
    }
	
	@Test
    public void testGetCountrySubscribers(){
    	
    }
	
	@Test
    public void testSetNotificationsStatus(){
		user.setNotifications(false);
		_model.updateUser(user);
		registrator.setNotificationsStatus(uId, true);
		user =  _model.getUserByUid(uId);
		assertEquals(true, user.getNotifications());
    }
	
	@Test
	public void testDeregisterFromNotifications(){
		
    }
	@Test
	public void testGetRegisteredAt(){
  
    }
	
	@Test
    public void testIsRegistrationExpired(){
    	
    }
	
	@Test
    public void testIsUserVerified1(){
    	user.setVerified(true);
    	_model.updateUser(user);
    	assertEquals(true, registrator.isUserVerified(uId));
    }
	
	@Test
    public void testIsUserVerified2(){
    	user.setVerified(false);
    	_model.updateUser(user);
    	assertEquals(false, registrator.isUserVerified(uId));
    }
}
