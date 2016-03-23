package models;

import java.util.List;
import java.util.UUID;

public interface Model {
	Country getCountry(String countryId);
	List<Country> getCountries();
	List<Vaccine> getCountryVaccines(Country country);
	List<PackingListItem> getCountryPackingListItems(Country country);
	User getUserByUid(UUID uId);
	User getUserByEmail(String email);
	void insertUser(UUID uid, String name, String email, String gender, Country[] countries);
	void setUserVerified(UUID uId);
	boolean isSubscribedToNotifications(UUID uId);
	List<String> getCountrySubscribers(String country);
	void setNotificationsStatus(UUID uId, boolean enable);
	void updateSubscribers(String country, List<String> emails);
	void updateUser(User user);
}