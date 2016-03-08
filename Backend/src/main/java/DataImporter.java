import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVReader;


public class DataImporter {

	public static void main(String[] args) {
		CSVReader reader;
		Map<String, String> countryIds;
		try {
			// Get the entire list of country names, links, and keys
			reader = new CSVReader(new FileReader("../data/CountriesAndLinks.csv"));
		     String [] nextLine;
		     countryIds = new HashMap<String, String>();
		     while ((nextLine = reader.readNext()) != null) {
		        // nextLine[] is an array of values from the line
		    	String countryName = nextLine[0];
		    	String url = "http://wwwnc.cdc.gov/" + nextLine[1];
		    	String[] urlParts = url.split("/");
		    	
		    	String countryKey = urlParts[urlParts.length - 1]; // ID
		    	countryIds.put(countryName, countryKey);
		     }	
		     
		     // Get the data for each country
		     reader = new CSVReader(new FileReader("../data/CountryData.csv"));
	    	 JSONParser parser = new JSONParser();
		     // packing list
		     while ((nextLine = reader.readNext()) != null) {
		    	 String packingListJSON = nextLine[0];
		    	 String recommendedVaccineJSON = nextLine[1];
		    	 String countryKey = countryIds.get(nextLine[2]);
		    	 // packing list

		    	 Object obj = parser.parse(packingListJSON);
		    	 JSONArray packingListItems = (JSONArray) obj;
		    	 
		 		Iterator<JSONObject> iterator = packingListItems.iterator();
				while (iterator.hasNext()) {
					JSONObject listItem = iterator.next();
					System.out.println(listItem.get("Category"));
					System.out.println(listItem.get("Item"));
					System.out.println(listItem.get("Notes"));
				}
				
				// vaccines
		    	obj = parser.parse(recommendedVaccineJSON);
		    	JSONArray vaccines = (JSONArray) obj;
		    	 
		 	    iterator = vaccines.iterator();
				while (iterator.hasNext()) {
					JSONObject listItem = iterator.next();
					System.out.println(listItem.get("DiseaseName"));
					System.out.println(listItem.get("Travelers"));
					System.out.println(listItem.get("Details"));
				}				
		    	 
				System.out.println(countryKey);
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
