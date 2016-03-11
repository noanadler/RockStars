package models;

import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oModel implements Model {
	private Sql2o sql2o;
	
    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

	@Override
	public Country getCountry(String countryId) {
        try (Connection conn = sql2o.open()) {
			List<Country> countries = conn.createQuery("select country, full_name from travel_information where country=:countryId")
					.addParameter("countryId", countryId)
	                .executeAndFetch(Country.class);
			Country country = countries.get(0);
			country.setVaccines(getCountryVaccines(countryId));
			//country.setItems(getCountryPackingListItems(countryId));

			return countries.get(0);
        }
	}

	@Override
	public List<Vaccine> getCountryVaccines(String countryId) {
        try (Connection conn = sql2o.open()) {
        	List<String[]> countryVaccineNames = conn.createQuery("select vaccines from travel_information where country=:countryId")
				.addParameter("countryId", countryId)
                .executeAndFetch(String[].class);
        	
            List<Vaccine> vaccines = conn.createQuery("select * from vaccines where name in :vaccines")  
    				.addParameter("vaccines", countryVaccineNames.get(0))
                    .executeAndFetch(Vaccine.class);
            return vaccines;
        }
	}

	@Override
	public List<PackingListItem> getCountryPackingListItems(String countryId) {
        try (Connection conn = sql2o.open()) {
            List<PackingListItem> packingListItems = conn.createQuery("select * from packing_list_items")
                    .executeAndFetch(PackingListItem.class);
            return packingListItems;
        }
	}

}
