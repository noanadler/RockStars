package models;

import java.util.List;
import java.util.UUID;

import javax.print.attribute.standard.PrinterLocation;

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
	
	@Override
	public User getUserByUid(UUID uId){
		try (Connection conn = sql2o.open()) {
			List<User> users = conn.createQuery("select * from users where uid = '" + uId + "'")  
	                .executeAndFetch(User.class);
			User user = users.get(0);
			return user;
		}
	}
	
	@Override
	public User getUserByEmail(String email){
		try (Connection conn = sql2o.open()) {
			List<User> users = conn.createQuery("select * from users where email = '" + email + "'")  
	                .executeAndFetch(User.class);
			if(users.size() > 0)
			{
				User user = users.get(0);
				return user;
			}else
			{
				return null;
			}
			
		}
	}
	
	@Override
	public UUID insertUser(String name, String email, String password, String gender, String[] countries){
		//TODO
		String insertSql = 
				"insert into users(id, name, email, password, gender, countries) " +
				"values (:uidParam, :nameParam, :emailParam, :passwordParam, :genderParam, :countriesParam)";
			UUID uuid = UUID.randomUUID();
			try (Connection conn = sql2o.open()) {
			    conn.createQuery(insertSql)
			    	.addParameter("uidParam", uuid)
			    	.addParameter("nameParam", name)
				    .addParameter("emailParam", email)
				    .addParameter("passwordParam", password)
				    .addParameter("genderParam", gender)
				    .addParameter("countriesParam", countries)
				    .executeUpdate();
			    return uuid;
			}
			
	}
	
	@Override
	public void setUserVerified(UUID uId){
		//TODO
	}
	
	@Override
	public boolean isSubscribedToNotifications(UUID uId){
		return false;
		//TODO
	}
	
	@Override
	public List<String> getCountrySubscribers(String country){
		return null;
		//TODO
	}
	
	@Override
	public void setNotificationsStatus(UUID uId, boolean enable){
		//TODO
	}
	
	@Override
	public void updateSubscribers(String country, List<String> email){
		//TODO
	}
	
	@Override
	public void updateUser(User user){
		//TODO
	}
	
	@Override
	public List<Alert> getCountryAlerts(Country country) {
        try (Connection conn = sql2o.open()) {
            List<Alert> alerts = conn.createQuery("select * from alerts where name in ('" + String.join(",", country.alert_names).replace(",", "','") + "')".replace(" '", "'"))
            		
                    .executeAndFetch(Alert.class);
            return alerts;
        }
	}

}
