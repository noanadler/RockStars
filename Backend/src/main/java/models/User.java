package models;

import java.util.Date;
import java.util.UUID;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;

public class User {
    UUID id;
    String fhir_id;
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
    public String getFHIRId(){return fhir_id;}
    public void setFHIRId(String fhir_id){this.fhir_id = fhir_id;}
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public String getEmail(){return email;}
    public String getHashedPassword(){return password;}
    public void setHashedPassword(String password){this.password = password;}
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
    public void setNotifications(boolean notifications){this.notification = notifications;}
    
    public void createFHIRPatientRecord(IGenericClient client)
    {
        Patient patient = new Patient();
        String system = "safetravels";
	    patient.addName().addFamily(name.split(" ")[1]).addGiven(name.split(" ")[0]);
	    if(gender.toLowerCase().equals("M")){patient.setGender(AdministrativeGenderEnum.MALE);}
	    else if(gender.toLowerCase().equals("F")){patient.setGender(AdministrativeGenderEnum.FEMALE);}
	    else{patient.setGender(AdministrativeGenderEnum.OTHER);}
	    MethodOutcome outcome = client.create()
	             .resource(patient)
	             .conditional()
	             .where(Patient.NAME.matches().value(name))
	             .execute();
	    System.out.println(outcome.getId().getIdPart());
        setFHIRId(outcome.getId().getIdPart());
    }
    public void createFHIRImmunizationRecord(IGenericClient client, Vaccine vaccine)
    {
        Immunization immunization = new Immunization();
        String system = "safetravels";
        //immunization.addIdentifier().setSystem("urn:" + system).setValue(UUID.randomUUID().toString());
        immunization.setPatient(new ResourceReferenceDt("Patient/" + fhir_id));
        immunization.setVaccineCode(new CodeableConceptDt(system, vaccine.getCode()));
        immunization.setStatus("completed");
        MethodOutcome outcome = client.create()
                .resource(immunization)
                .execute();
    }

}
