package services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

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
	private String country = "Brazil";
	private String[] countries = {country};	
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
	public void testBeginRegistration1() {
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
		StringBuilder errorReporter = new StringBuilder();
		UUID uuid = registrator.beginRegistration(userEmail, userPassword, userName, userGender, errorReporter);
		assertNotNull(uuid);

	}
	
	/*
	 * When user exist beginRegistration should return null
	 */
	@Test
	public void testBeginRegistration2() {
		StringBuilder errorReporter = new StringBuilder();
		UUID uuid = registrator.beginRegistration(userEmail, userPassword, userName, userGender, errorReporter);
		assertEquals(null, uuid);
	}
	

	@Test
	public void testUidExists1() {
        assertTrue(registrator.uIdExists(uId));
	}
	

	@Test
	public void testUidExists2() {
		UUID falseUID = UUID.randomUUID();
		assertFalse(registrator.uIdExists(falseUID));
	}
	
	@Test
	public void testIsSubscribedToNotifications1(){
		user.setNotifications(true);
		_model.updateUser(user);
		assertTrue(registrator.isSubscribedToNotifications(uId));
    }
	
	@Test
	public void testIsSubscribedToNotifications2(){
		user.setNotifications(false);
		_model.updateUser(user);
		assertFalse(registrator.isSubscribedToNotifications(uId));
    }
	
	@Test
    public void testVerifyUser(){
    	user.setVerified(false);
    	_model.updateUser(user);
    	registrator.verifyUser(uId);
    	user =  _model.getUserByUid(uId);
    	assertTrue(user.getVerified());
    }
	
	@Test
    public void testEmailExists1(){
		assertTrue(registrator.emailExists(userEmail));
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
		assertFalse(registrator.emailExists(userEmail));
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
		
	 //This test fails due to issue #2 - ql2oModel.getCountrySubscribers throws the following exception:
	 //Database error: ERROR: relation "country_lists" does not exist
	@Test
    public void testUpdateNotificationCountries(){
		String newCountry = "Mexico";
    	String[] selectedCountries = {newCountry};
    	registrator.updateNotificationCountries(userEmail, selectedCountries);
    	assertEquals(newCountry, user.getCountries());	
    }	

	 
	@Test
    public void testGetCountrySubscribers(){
		List<String> countrySub = _model.getCountrySubscribers(country);
		assertEquals(countrySub, registrator.getCountrySubscribers(country));
    }
	
	@Test
    public void testSetNotificationsStatus(){
		user.setNotifications(false);
		_model.updateUser(user);
		registrator.setNotificationsStatus(uId, true);
		user =  _model.getUserByUid(uId);
		assertTrue(user.getNotifications());
    }
	
	 //This test fails due to issue #2 - ql2oModel.getCountrySubscribers throws the following exception:
	 //Database error: ERROR: relation "country_lists" does not exist
	@Test
	public void testDeregisterFromNotifications(){
		Boolean isSubscribed = false;
		String[] countries = user.getCountries();
		registrator.deregisterFromNotifications(uId, false);
		for(String c : countries) {
			List<String> subs = _model.getCountrySubscribers(c);
			for(String s : subs) {
				if(s.equals(userEmail)) {
					isSubscribed = true;
				}
			}
		}
		assertTrue(isSubscribed);

    }
	@Test
	public void testGetRegisteredAt(){
		Date registred = user.getRegistered_at();
		assertEquals(registred, registrator.getRegisteredAt(uId));
    }
	
	/*
	 * Testing registration that is not expired
	 */
	@Test
    public void testIsRegistrationExpired1(){
		Date today = new Date();
    	user.setRegistered_at(today);
    	_model.updateUser(user);
    	assertEquals(false, registrator.isRegistrationExpired(uId));
    }
	
	/*
	 * Testing registration that is expired
	 * This method fails due to issue #3
	 */
	@Test
    public void testIsRegistrationExpired2(){
		LocalDate date = LocalDate.of( 2011 , Month.FEBRUARY , 11 );
		Date moreThenYearAgo = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    	user.setRegistered_at(moreThenYearAgo);
    	_model.updateUser(user);
    	assertEquals(true, registrator.isRegistrationExpired(user.getId()));

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

