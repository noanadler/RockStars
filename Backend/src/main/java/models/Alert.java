package models;

import java.util.Date;

public class Alert {
	int id;
	String country_id;
	String title;
	String description;
	Date started_at;
	Date ended_at;
	
	public String getCountry_id(){
		return country_id;
	}
	public void setCountry_id(String country_id){
		this.country_id = country_id;
	}
	public String getTitle(){
		return title;
	}
	public void setTitle(String title){
		this.title = title;
	}
	public String getDescription(){
		return description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public Date getStarted_at(){
		return started_at;
	}
	public void setStarted_at(Date started_at){
		this.started_at = started_at;
	}
}
