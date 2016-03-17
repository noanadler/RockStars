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
			List<Country> countries = conn.createQuery("select country, full_name, vaccines as vaccine_names, packing_list as packinglist_names from travel_information where country=:countryId")
					.addParameter("countryId", countryId)
	                .executeAndFetch(Country.class);
			Country country = countries.get(0);
			country.setVaccines(getCountryVaccines(country));
			country.setItems(getCountryPackingListItems(country));
			return country;
        }
	}
	
	@Override
	public List<Country> getCountries() {
        try (Connection conn = sql2o.open()) {
			List<Country> countries = conn.createQuery("select country, full_name from travel_information")
	                .executeAndFetch(Country.class);
			return countries;
        }
	}

	@Override
	public List<Vaccine> getCountryVaccines(Country country) {
        try (Connection conn = sql2o.open()) {

            List<Vaccine> vaccines = conn.createQuery("select * from vaccines where name in ('" + String.join(",", country.vaccine_names).replace(",", "','") + "')".replace(" '", "'"))  
    				//.addParameter("vaccines", "'" + String.join(",", country.vaccine_names).replace(",", "','").replace(" '", "'") + "'")
                    .executeAndFetch(Vaccine.class);
            return vaccines;
        }
	}

	@Override
	public List<PackingListItem> getCountryPackingListItems(Country country) {
        try (Connection conn = sql2o.open()) {
            List<PackingListItem> packingListItems = conn.createQuery("select * from packing_list_items where name in ('" + String.join(",", country.packinglist_names).replace(",", "','") + "')".replace(" '", "'"))
            		//.addParameter("listitems", String.join(",", country.packinglist_names))
                    .executeAndFetch(PackingListItem.class);
            return packingListItems;
        }
	}

}
