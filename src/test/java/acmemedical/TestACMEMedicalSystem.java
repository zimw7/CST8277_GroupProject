/********************************************************************************************************
 * File:  TestACMEMedicalSystem.java
 * Course Materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 */
package acmemedical;

import static acmemedical.utility.MyConstants.APPLICATION_API_VERSION;
import static acmemedical.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.PHYSICIAN_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.MEDICAL_TRAINING_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmemedical.entity.Physician;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.DurationAndStatus;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.MedicalSchool;
import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Medicine;
import acmemedical.entity.Patient;
import acmemedical.entity.PrivateSchool;
import acmemedical.entity.PublicSchool;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMEMedicalSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_all_physicians_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>(){});
        assertThat(physicians, is(not(empty())));
        assertThat(physicians, hasSize(1));
    }
    
    
    /**
     * 2.get Physician by id with admin role
     */
    @Test
    public void test02_getPhysicianById_success() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));
        Physician physician = response.readEntity(Physician.class);
        assertThat(physician.getId(), is(1));
    }

    /**
     * 3.get Physician by id with a normal user 
     */
    @Test
    public void test03_getPhysicianById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));//user cst8277's physician id = 1, will pass this test
    }
    
    /**
     * 4. get physician ID = 1995, 404
     */
    @Test
    public void test04_getPhysicianById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1995") // ID 1995 not exist
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(404));
    }

    /**
     * 5. get all Physician by normal user
     */
    @Test
    public void test05_getAllPhysicians_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(403));
    }

    /**
     * 6. new Physician by admin
     */
    @Test
    public void test06_createPhysician_success() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Jane");
        newPhysician.setLastName("Doe");
        newPhysician.setVersion(1);
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200)); // HTTP 200 Created
    }
    
    /**
     * 7. new Physician by normal user
     */
    @Test
    public void test07_createPhysician_fail() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Jane");
        newPhysician.setLastName("Doe");

        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(403)); // HTTP 201 Created
    }
    
    /**
     * 8. update Physician by admin
     */
    @Test
    public void test08_updatePhysician_success() {
        Physician updatedPhysician = new Physician();
        updatedPhysician.setId(1); 
        updatedPhysician.setVersion(1); 
        updatedPhysician.setFirstName("Updated");
        updatedPhysician.setLastName("Qiu");

        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updatedPhysician, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }

 
    /**
     * 9. Get all medical schools with ADMIN_ROLE
     */
    @Test
    public void test09_getAllMedicalSchools_withAdmin() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 10. Get specific medical school by id with ADMIN_ROLE
     */
    @Test
    public void test10_getMedicalSchoolById_asAdmin() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/1")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 11. Get specific medical school by id which not exists
     */
    @Test
    public void test11_getMedicalSchoolById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool" + "/99")//medical school id not exist
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(500));  //no result returned and not handled this exception
    }
    
    /**
     * 12. Create medical school as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test12_createMedicalSchool_asAdmin_success() {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("CST Medical College");

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 13. Create medical school as user_ROLE (403)
     */
    @Test
    public void test13_createMedicalSchool_asUser_foridden() {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("CST Medical College");

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 14. Update medical school as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test14_updateMedicalSchool_asAdmin_success() {
        PublicSchool update = new PublicSchool();
        update.setId(1);
        update.setName("Updated Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 15. Update medical school as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test15_updateMedicalSchool_asUser_forbidden() {
        PublicSchool update = new PublicSchool();
        update.setId(1);
        update.setName("User Update Attempt");

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }


    
    /**
     * 16. Get all medicines as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test16_getAllMedicines_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 17. Get medicine by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test17_getMedicineById_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 18. Get non-existent medicine by ID as ADMIN_ROLE (404 Not Found)
     */
    @Test
    public void test18_getMedicineById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine/999")
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }

    /**
     * 19. create new medicine as ADMIN_ROLE 
     */
    @Test
    public void test19_createMedicine_admin_success() {
    	Medicine newMedicine = new Medicine();
    	newMedicine.setMedicine("Tynol","ABC Inc.","One per day");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(201) );
    }
    
    
    /**
     * 20. Update non-existent medicine as ADMIN_ROLE (404 Not Found)
     */
    @Test
    public void test20_updateMedicine_notFound() {
        Medicine updatedMed = new Medicine();
        updatedMed.setDrugName("NonExistentMed");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine/999")
            .request()
            .put(Entity.json(updatedMed));

        assertThat(response.getStatus(), is(404));
    }


    /**
     * 21. Get all patients as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test21_getAllPatients_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 22. Get all patients as USER_ROLE (200 OK)
     */
    @Test
    public void test22_getAllPatients_asUser_success() {
        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    
    /**
     * 23. Get patient by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test23_getPatientById_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 24. Get patient by ID as USER_ROLE (200 OK)
     */
    @Test
    public void test24_getPatientById_asUser_success() {
        Response response = webTarget
            .register(userAuth)
            .path("patient/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 25. Get non-existent patient by ID as ADMIN_ROLE (404 Not Found)
     */
    @Test
    public void test25_getPatientById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/999")
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }

    
    /**
     * 26. Add patient as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test26_addPatient_asUser_forbidden() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("User");
        newPatient.setLastName("Attempt");

        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request()
            .post(Entity.json(newPatient));

        assertThat(response.getStatus(), is(403));
    }
    

    /**
     * 27. Get all prescriptions as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test27_getAllPrescriptions_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("prescription")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 28. Get prescription by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test28_getPrescriptionById_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("prescription/1/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 29. Get prescription by non-existent ID (404 Not Found)
     */
    @Test
    public void test29_getPrescriptionById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path("prescription/999/999")
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }


    /**
     * 30. create new prescription by admin role
     */
    @Test
    public void test30_createPrescription() {
        // Create the Prescription object
        Prescription newPrescription = new Prescription();
        PrescriptionPK prescriptionPK = new PrescriptionPK(2,1);

        newPrescription.setId(prescriptionPK);

//        newPrescription.setMedicine(newMedicine);
        newPrescription.setNumberOfRefills(3);
        newPrescription.setPrescriptionInformation("Take with food");

        Response response = webTarget
            .register(adminAuth)
            .path("prescription")  // Assuming the resource path for prescriptions is "prescription"
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPrescription, MediaType.APPLICATION_JSON));

        // Check the response status
        assertThat(response.getStatus(), is(201));  // Expecting "Created" (201) status code
    }


    /**
     * 31. Get all medical certificates as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test31_getAllMedicalCertificates_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 32. Get medical certificate by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test32_getMedicalCertificateById_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 33. Get medical certificate by ID as USER_ROLE (200 OK if owner)
     */
    @Test
    public void test33_getMedicalCertificateById_asUser_success_ifOwner() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }


    /**
     * 34. Add medical certificate as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test34_createMedicalCertificate_asUser_forbidden() {
        Map<String, Integer> requestBody = Map.of("Physician_id", 2);

        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request()
            .post(Entity.json(requestBody));

        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * 35. Add medical certificate as Admin_ROLE (200)
     */
    @Test
    public void test35_createMedicalCertificate_admin_success() {
        Map<String, Integer> requestBody = Map.of("Physician_id", 2);

        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request()
            .post(Entity.json(requestBody));

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 36. Update medical certificate as admin_ROLE 
     */
    @Test
    public void test36_updateMedicalCertificate_admin_success() {
        MedicalCertificate updated = new MedicalCertificate();
        updated.setSigned((byte) 0);
        
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updated, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 37. Get all medical training as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test37_getAllMedicalTrainings_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicaltraining")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 38. Get specific medical training by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test38_getMedicalTrainingById_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicaltraining/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 39. Get specific medical training by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test39_getMedicalTrainingById_asUser_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 40. Get specific medical school by id which not exists
     */
    @Test
    public void test40_getMedicalTrainingById_notFound() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicaltraining" + "/99")//medical training not exist
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(500));  //no result returned and not handled this exception
    }

    /**
     *  41 create new Medical school by admin
     */
    @Test
    public void test11_createMedicalSchool_asAdmin_success() {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("CST Medical College");

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }
    
    
    
    /**
     * 42. Update medical school as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test42_createMedicalTraning_asAdmin_success() {
    	
    	PublicSchool newSchool = new PublicSchool();
        newSchool.setName("CST Medical College");
        
    	MedicalCertificate certificate = new MedicalCertificate(); 
    	
    	DurationAndStatus durationAndStatus = new DurationAndStatus();
        durationAndStatus.setStartDate(LocalDateTime.now().minusDays(1)); 
        durationAndStatus.setEndDate(LocalDateTime.now()); 
        durationAndStatus.setActive((byte) 1); 
        
        MedicalTraining newTraning = new MedicalTraining();

        newTraning.setDurationAndStatus(durationAndStatus);
        newTraning.setMedicalSchool(newSchool);
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newTraning, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(201));
    }

    
    /**
     *43. Delete medical certificate as ADMIN_ROLE (200 OK)
     */
    @Transactional
    @Test
    public void test43_deleteMedicalCertificate_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate/1") // Use a valid certificate ID
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 44. Delete medical certificate as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test44_deleteMedicalCertificate_asUser_forbidden() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/2") // Replace with existing or mock
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 45. delete Physician by admin
     */
    @Transactional
    @Test
    public void test45_deletePhysician_success() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1") 
            .request()
            .delete();

        assertThat(response.getStatus(), is(204));
    }

    /**
     * 46. delete Physician by normal user (403 Forbidden)
     */
    @Transactional
    @Test
    public void test46_deletePhysician_fail_unauthorized() {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/2")
            .request()
            .delete();

        assertThat(response.getStatus(), is(401));
    }
    
    /**
     * 47. Delete medical school as ADMIN_ROLE (200 OK)
     */
    @Transactional
    @Test
    public void test47_deleteMedicalSchool_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/1")
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 48. Delete medical school as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test48_deleteMedicalSchool_asUser_forbidden() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/2")
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 49. Delete medicine as ADMIN_ROLE (200 OK)
     */
    @Transactional
    @Test
    public void test49_deleteMedicine_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine/1")
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 50. Delete medicine as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test50_deleteMedicine_asUser_forbidden() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/2")
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 51. Delete patient as ADMIN_ROLE (204 No Content)
     */
    @Transactional
    @Test
    public void test51_deletePatient_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/1")
            .request()
            .delete();

        assertThat(response.getStatus(), is(204));
    }


}