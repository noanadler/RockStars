import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import com.opencsv.CSVReader;

import data.HerokuDataSource;


public class DataImporter {

	public static void main(String[] args) {
		CSVReader reader;
		Map<String, String> countryIds;
		Map<String, String> countryUrls;

    	Sql2o sql2o = new Sql2o(new HerokuDataSource());
    	String insertPackingItemsSQL = "INSERT INTO packing_list_items(name, category, notes) " +
    			"VALUES (:name, :category, :notes)";
    	
    	String vaccinesSQL = "INSERT INTO vaccines(name, category, notes) " +
    			"VALUES (:name, :category, :notes)";    
    	    	
		try {
	    	Connection con = sql2o.open();
			// Get the entire list of country names, links, and keys
			reader = new CSVReader(new FileReader("../data/CountriesAndLinks.csv"));
		     String [] nextLine;
		     countryIds = new HashMap<String, String>();
		     countryUrls = new HashMap<String, String>();

		     while ((nextLine = reader.readNext()) != null) {
		        // nextLine[] is an array of values from the line
		    	String countryName = nextLine[0];
		    	String url = "http://wwwnc.cdc.gov/" + nextLine[1];
		    	String[] urlParts = url.split("/");
		    	
		    	String countryKey = urlParts[urlParts.length - 1]; // ID
		    	countryIds.put(countryName, countryKey);
		    	countryUrls.put(countryName, url);
		     }	
		     
		     // Get the data for each country
		     reader = new CSVReader(new FileReader("../data/CountryData.csv"));
	    	 JSONParser parser = new JSONParser();
		     // packing list
		     while ((nextLine = reader.readNext()) != null) {
		    	 String packingListJSON = nextLine[0];
		    	 String recommendedVaccineJSON = nextLine[1];
		    	 String fullCountryName = nextLine[2];
		    	 String countryKey = countryIds.get(nextLine[2]);
		    	 String countryUrl = countryUrls.get(nextLine[2]);
		    	 
		    	 // store the names to insert with the country into travel information
		    	 List<String> packingListItemNames = new ArrayList<String>();
		    	 List<String> vaccineNames = new ArrayList<String>();

		    	 // packing list

		    	 Object obj = parser.parse(packingListJSON);
		    	 JSONArray packingListItems = (JSONArray) obj;
		    	 
		 		Iterator<JSONObject> iterator = packingListItems.iterator();
				while (iterator.hasNext()) {
					JSONObject listItem = iterator.next();
					packingListItemNames.add("\"" + (String)listItem.get("Item") + "\"");
					try {
						con.createQuery(insertPackingItemsSQL)
				        .addParameter("name", listItem.get("Item"))
				        .addParameter("category", listItem.get("Category"))
				        .addParameter("notes", listItem.get("Notes"))
				        .executeUpdate();
					} catch (Sql2oException e) {
						//System.out.println(e.getMessage());
					}
				}
				
				// vaccines
		    	obj = parser.parse(recommendedVaccineJSON);
		    	JSONArray vaccines = (JSONArray) obj;
		    	 
		 	    iterator = vaccines.iterator();
				while (iterator.hasNext()) {
					JSONObject listItem = iterator.next();
					vaccineNames.add("\"" + (String)listItem.get("DiseaseName") + "\"");
					try {
						con.createQuery(vaccinesSQL)
				        .addParameter("name", listItem.get("DiseaseName"))
				        .addParameter("category", listItem.get("Travelers"))
				        .addParameter("notes", listItem.get("Details"))
				        .executeUpdate();	
					} catch (Sql2oException e) {
						//System.out.println(e.getMessage());
					}						
				}				
								
		    	String travelInformationSQL = "INSERT INTO travel_information(country, full_name, vaccines, packing_list) " +
		    			"VALUES (:country, :full_name, '{" + vaccineNames.toString().replace("[", "").replace("]", "") + "}', '{" + packingListItemNames.toString().replace("[", "").replace("]", "") + "}')";   
				try {
			    	con.createQuery(travelInformationSQL)
			        .addParameter("country", countryKey)
			        .addParameter("full_name", fullCountryName)
			        .executeUpdate();	
				} catch (Sql2oException e) {
					System.out.println(e.getMessage());
				}	
				
				System.out.println("Added " + fullCountryName + " to the database");
		     }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("Parse Exception!");
			e.printStackTrace();
		}
	}
}
