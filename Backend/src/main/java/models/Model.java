package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface Model {
	Country getCountry(String countryId);
	List<Country> getCountries();
	List<User> getUsers();
	List<Vaccine> getUserVaccines(User user);
	List<Vaccine> getCountryVaccines(Country country);
	List<PackingListItem> getCountryPackingListItems(Country country);
	User getUserByUid(UUID uId);
	User getUserByEmail(String email);
	UUID insertUser(String name, String email, String password, String gender, String[] countries,
			boolean verified, boolean notification);
	void setUserVerified(UUID uId);
	void addVaccineToUser(String vaccineName, UUID userId, Date date);
	boolean isSubscribedToNotifications(UUID uId);
	List<String> getCountrySubscribers(String country);
	void setNotificationsStatus(UUID uId, boolean enable);
	void updateSubscribers(String country, List<String> emails);
	void updateUser(User user);
	List<Alert> getCountryAlerts(Country country);
	List<String> getDistinctAlertCountries();
	void clearAlerts();
	void updateFhirId(User user);
}