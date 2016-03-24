package models;

import java.util.Date;
import java.util.UUID;

public class User {
    UUID id;
    String name;
    String email;
    String gender;
    String password;
    Date registered_at;
    Date birthdate;
    String[] countries;
    boolean verified;
    boolean notification;

    public UUID getId(){ return id;}
    public void setId(UUID id){this.id = id;}
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public String getEmail(){return email;}
    public String getHashedPassword(){return password;}
    public void setEmail(String email){this.email = email;}
    public String getGender(){return gender;}
    public void setGender(String gender){this.gender = gender;}
    public Date getRegistered_at(){return registered_at;}
    public void setRegistered_at(Date registered_at){ this.registered_at = registered_at;}
    public String[] getCountries(){return countries;}
    public void setCountries(String[] countries){
    	if(countries != null)
    	{
	        this.countries = new String[countries.length];
	        System.arraycopy( countries, 0, this.countries, 0, countries.length);
    	}
    }
    public boolean getVerified(){return verified;}
    public void setVerified(boolean verified){this.verified = verified;}
    public boolean getNotifications(){return notification;}
    public void setNotifications(boolean notifications){this.notification = notification;}

}
