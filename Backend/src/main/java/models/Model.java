package models;

import java.util.List;

public interface Model {
	Country getCountry(String countryId);
	List<Vaccine> getCountryVaccines(String countryId);
	List<PackingListItem> getCountryPackingListItems(String countryId);	
}