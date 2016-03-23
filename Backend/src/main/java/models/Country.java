package models;

import java.util.List;

public class Country {
	String country;
	String full_name;
	String[] vaccine_names;
	String[] packinglist_names;
	String[] alert_names;
	

	List<Vaccine> vaccines;
	List<PackingListItem> items;
	List<Alert> alerts;

	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getFullName() {
		return full_name;
	}
	public void setFullName(String full_name) {
		this.full_name = full_name;
	}
	public List<Vaccine> getVaccines() {
		return vaccines;
	}
	public void setVaccines(List<Vaccine> vaccines) {
		this.vaccines = vaccines;
	}
	public List<PackingListItem> getItems() {
		return items;
	}
	public void setItems(List<PackingListItem> items) {
		this.items = items;
	}
	public List<Alert> getAlerts() {
		return alerts;
	}
	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

}
