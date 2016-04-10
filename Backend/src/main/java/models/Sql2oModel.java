package models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

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
			country.setAlerts(getCountryAlerts(country));

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
	public List<Vaccine> getUserVaccines(User user) {
        try (Connection conn = sql2o.open()) {

            List<Vaccine> vaccines = conn.createQuery("select vaccines.*, to_char(completed_vaccines.vaccinated_date, 'DD/MM/YYYY') as vaccinated_date from vaccines, completed_vaccines where vaccines.name=completed_vaccines.vaccine_id AND user_id=:uid")  
    				.addParameter("uid", user.getId())
                    .executeAndFetch(Vaccine.class);
            return vaccines;
        }
	}	
	
	public Vaccine getVaccineByName(String vaccineName){
		try (Connection conn = sql2o.open()) {
			List<Vaccine> vaccines = conn.createQuery("select * from vaccines where name = '" + vaccineName + "'")  
	                .executeAndFetch(Vaccine.class);
			Vaccine vaccine = vaccines.get(0);
	        return vaccine;
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
            		//.addParameter("listtems", String.join(",", country.packinglist_names))
                    .executeAndFetch(PackingListItem.class);
            return packingListItems;
        }
	}
	
	@Override
	public List<User> getUsers() {
        try (Connection conn = sql2o.open()) {
        	List<User> users = conn.createQuery("select * from users")  
	                .executeAndFetch(User.class);
            return users;
        }
	}
	
	@Override
	public User getUserByUid(UUID uId){
		try (Connection conn = sql2o.open()) {
			List<User> users = conn.createQuery("select * from users where id = '" + uId + "'")  
	                .executeAndFetch(User.class);
			User user = users.get(0);
			user.setVaccines(getUserVaccines(user));
			return user;
		}
	}
	
	@Override
	public User getUserByEmail(String email){
		try (Connection conn = sql2o.open()) {
			String userQuery = "select * from users where email = '" + email + "'";
			List<User> users = conn.createQuery(userQuery)  
	                .executeAndFetch(User.class);
			if(users.size() > 0)
			{
				User user = users.get(0);
				user.setVaccines(getUserVaccines(user));
				return user;
			}else
			{
				return null;
			}
			
		}
	}
	
	@Override
	public UUID insertUser(String name, String email, String password, String gender, String[] countries, 
			boolean verified, boolean notification){
		String insertSql = 
				"insert into users(id, name, email, password, gender, countries, registered_at, verified, notification) " +
				"values (:uidParam, :nameParam, :emailParam, :passwordParam, :genderParam,"
				+ " :countriesParam, :registered_atParam, :verifiedParam, :notificationParam)";
			UUID uuid = UUID.randomUUID();
			Date date = new Date();
			try (Connection conn = sql2o.open()) {
			    conn.createQuery(insertSql)
			    	.addParameter("uidParam", uuid)
			    	.addParameter("nameParam", name)
				    .addParameter("emailParam", email)
				    .addParameter("passwordParam", password)
				    .addParameter("genderParam", gender)
				    .addParameter("countriesParam", countries)
				    .addParameter("registered_atParam", date)
				    .addParameter("verifiedParam", verified)
				    .addParameter("notificationParam", notification)
				    .executeUpdate();
			    return uuid;
			}		
	}
	
	@Override
	public void setUserVerified(UUID uId){
		String updateSql = "update users set verified = TRUE where id = :uIdParam";
        try (Connection conn = sql2o.open()) {
         	 conn.createQuery(updateSql)
         			.addParameter("uidParam", uId)
  	                .executeUpdate();
         }
	}
	
	@Override
	public void addVaccineToUser(String vaccineName, UUID userId, Date date) {
		String insertSql = "insert into completed_vaccines (user_id, vaccine_id, vaccinated_date) values (:uuid, :vaccine, :date)";
        try (Connection conn = sql2o.open()) {
        	 conn.createQuery(insertSql)
        			.addParameter("uuid", userId)
        			.addParameter("vaccine", vaccineName)
        			.addParameter("date", date)
 	                .executeUpdate();
        }
	}

	
	@Override
	public boolean isSubscribedToNotifications(UUID uId){
        try (Connection conn = sql2o.open()) {
        	List<User> users = conn.createQuery("select notifications from users where id = :uIdParam")
       			 	.addParameter("uIdParam", uId)
	                .executeAndFetch(User.class);
        	 User user = users.get(0);
			 return user.notification;
       }
	}
	
	@Override
	public List<String> getCountrySubscribers(String country){
		String selectSql = "SELECT emails from country_lists where country=:countryParam";
        try (Connection conn = sql2o.open()) {
            List<String> addresses = conn.createQuery(selectSql)
        		.addParameter("countryParam", country)
        		.executeAndFetch(String.class);
           return addresses.size() == 0? addresses : Arrays.asList(addresses.get(0).split(";"));     
         }
	}
	
	@Override
	public void setNotificationsStatus(UUID uId, boolean enable){
		String updateSql = "update users set notifications = :enableParam where id = :uIdParam";
        try (Connection conn = sql2o.open()) {
         	 conn.createQuery(updateSql)
         			.addParameter("uidParam", uId)
         			.addParameter("enableParam", enable)
  	                .executeUpdate();
         }
	}
	
	@Override
	public void updateSubscribers(String country, List<String> emails){
		String updateSql = "update country_lists set email = :emailParam where country = :countryParam";
		String emailsStr = String.join(";", emails);
		try (Connection conn = sql2o.open()) {
         	 conn.createQuery(updateSql)
         			.addParameter("emailParam", emailsStr)
         			.addParameter("countryParam", country)
  	                .executeUpdate();
         }
	}
	
	@Override
	public void updateUser(User user){
		String updateSql = 
				"update users SET name = :nameParam, email = :emailParam, password = :passwordParam, gender = :genderParam, countries = :countriesParam, verified = :verifiedParam, "
				+ "notification = :notificationParam where id = :uidParam";
			
			try (Connection conn = sql2o.open()) {
			    conn.createQuery(updateSql)
			    	.addParameter("uidParam", user.id)	
			    	.addParameter("nameParam", user.name)
				    .addParameter("emailParam", user.email)
				    .addParameter("passwordParam", user.password)
				    .addParameter("genderParam", user.gender)
				    .addParameter("countriesParam", user.countries)
				    .addParameter("verifiedParam", user.verified)
				    .addParameter("notificationParam", user.notification)
				    .executeUpdate();
			}
	}
	
	@Override
	public void updateFhirId(User user){
		String updateSql = 
				"update users SET fhir_id = :fhirParam where id = :uidParam";
			
			try (Connection conn = sql2o.open()) {
			    conn.createQuery(updateSql)
			    	.addParameter("uidParam", user.id)	
			    	.addParameter("fhirParam", user.fhir_id)
				    .executeUpdate();
			}
	}
	
	@Override
	public List<Alert> getCountryAlerts(Country country) {
        try (Connection conn = sql2o.open()) {
            List<Alert> alerts = conn.createQuery("select * from alerts where country_id=:country")
        		.addParameter("country", country.getFullName())
                .executeAndFetch(Alert.class);
            return alerts;
        }
	}
	
	@Override
	public List<String> getDistinctAlertCountries(){
		 String getDistinctsql = "SELECT distinct country_id from alerts";
        try (Connection conn = sql2o.open()) {
            List<String> countries = conn.createQuery(getDistinctsql)
        		  .executeAndFetch(String.class);
            return countries;        
        }
	}
	
	@Override
	public void clearAlerts(){
		//Clear all records from the table
        try (Connection conn = sql2o.open()) {
             conn.createQuery("delete from alerts").executeUpdate();
        }		
	}

}
