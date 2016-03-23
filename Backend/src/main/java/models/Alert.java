package models;

import java.util.Date;

public class Alert {
	String alertTitle;
	String alertSummary;
	String alertStatus;
	String date;
	Date alertDate;
	
	public String getAlertTitle() {
		return alertTitle;
	}
	public void setAlertTitle(String alertTitle) {
		this.alertTitle = alertTitle;
	}
	public String getAlertSummary() {
		return alertSummary;
	}
	public void setAlertSummary(String alertSummary) {
		this.alertSummary = alertSummary;
	}
	public String getAlertStatus() {
		return alertStatus;
	}
	public void setAlertStatus(String alertStatus) {
		this.alertStatus = alertStatus;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Date getAlertDate() {
		return alertDate;
	}
	public void setAlertDate(Date alertDate) {
		this.alertDate = alertDate;
	}

}
