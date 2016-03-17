package models;

import java.util.List;

public interface Model {
	Country getCountry(String countryId);
	List<Country> getCountries();
	List<Vaccine> getCountryVaccines(Country country);
	List<PackingListItem> getCountryPackingListItems(Country country);	
}