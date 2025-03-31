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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import acmemedical.ejb.ACMEMedicalService;
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

    @PersistenceContext
    private EntityManager em;
    
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
//        assertThat(physicians, hasSize(1));
    }
    
    @Test
    public void test02_all_physicians_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 3.get Physician by id with admin role
     */
    @Test
    public void test03_getPhysicianById_admin_success() {
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
     * 4.get Physician by id with a normal user 
     */
    @Test
    public void test04_getPhysicianById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));//user cst8277's physician id = 1, will pass this test
    }

    /**
     * 5. new Physician by admin
     */
    @Test
    public void test05_createPhysician_admin_success() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Michael");
        newPhysician.setLastName("Smith");
        newPhysician.setVersion(1);
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200)); 
    }
    
    /**
     * 6. new Physician by normal user
     */
    @Test
    public void test06_createPhysician_user_fail() {
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
     * 7. update Physician by admin
     */
    @Test
    public void test07_updatePhysician_admin_success() {
        Physician updatedPhysician = new Physician();
        updatedPhysician.setId(1); 
        updatedPhysician.setVersion(1); 
        updatedPhysician.setFirstName("Updated");
        updatedPhysician.setLastName("updated");

        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updatedPhysician, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 08. delete Physician by normal user (403 Forbidden)
     */
    @Transactional
    @Test
    public void test08_deletePhysician_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/3")
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }

    /**
     * 09. delete Physician by admin role (204)
     */
    @Transactional
    @Test
    public void test09_deletePhysician_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME + "/5") 
            .request()
            .delete();

        assertThat(response.getStatus(), is(204));
    }
 
    /**
     * 10. Get all patients as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test10_getAllPatients_Admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 11. Get all patients as USER_ROLE (200 OK)
     */
    @Test
    public void test11_getAllPatients_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    
    /**
     * 12. Get patient by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test12_getPatientById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 13. Get patient by ID as USER_ROLE (200 OK)
     */
    @Test
    public void test13_getPatientById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("patient/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 14. Add patient as user_ROLE (403 Forbidden)
     */
    @Test
    public void test14_addPatient_user_fail() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Admin");
        newPatient.setLastName("Attempt");

        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request()
            .post(Entity.json(newPatient));

        assertThat(response.getStatus(), is(403));
    }

    
    /**
     * 15. Add patient as Admin_ROLE 
     */
    @Test
    public void test15_addPatient_admin_success() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Charles");
        newPatient.setLastName("Xavier");
        newPatient.setYear(1978);
        newPatient.setAddress("456 Main St. Toronto");
        newPatient.setHeight(170);
        newPatient.setWeight(90);

        Response response = webTarget
            .register(adminAuth)
            .path("patient")
            .request()
            .post(Entity.json(newPatient));

        assertThat(response.getStatus(), anyOf(is(200), is(201)));
    }
    
    /**
     * 16. Update patient as Admin (200 OK)
     */
    @Test
    public void test16_updatePatient_admin_success() {
        Patient update = new Patient();
        update.setFirstName("Charles_updated");
        update.setLastName("Xavier_updated");
        update.setYear(1978);
        update.setAddress("456 Main St. Toronto");
        update.setHeight(170);
        update.setWeight(90);

        Response response = webTarget
            .register(adminAuth)
            .path("patient/3")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 17. Update patient as User (403 Forbidden)
     */
    @Test
    public void test17_updatePatient_user_fail() {
        Patient update = new Patient();
        update.setFirstName("AdminY");
        update.setLastName("AttemptY");

        Response response = webTarget
            .register(userAuth)
            .path("patient/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    /**
     * 18. Delete patient as user_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test18_deletePatient_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path("patient/4")
            .request()
            .delete();

        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * 19. Delete patient as ADMIN_ROLE (204 No Content)
     */
    @Transactional
    @Test
    public void test19_deletePatient_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/4")
            .request()
            .delete();

        assertThat(response.getStatus(), is(204));
    }
    
    /**
     * 20. Get all medicines as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test20_getAllMedicines_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 21. Get all medicines as User_ROLE (200 OK)
     */
    @Test
    public void test21_getAllMedicines_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    
    /**
     * 22. Get medicine by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test22_getMedicineById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 23. Get medicine by ID as User_ROLE (200 OK)
     */
    @Test
    public void test23_getMedicineById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 24. create new medicine as User_ROLE (403)
     */
    @Test
    public void test24_createMedicine_user_fail() {
    	Medicine newMedicine = new Medicine();
    	newMedicine.setMedicine("Tynol","ABC Inc.","One per day");

        Response response = webTarget
            .register(userAuth)
            .path("medicine")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(403) );
    }
    /**
     * 25. create new medicine as ADMIN_ROLE (201)
     */
    @Test
    public void test25_createMedicine_admin_success() {
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
     * 26. Update a medicine as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test26_updateMedicine_admin_success() {
        Medicine updatedMed = new Medicine();
        updatedMed.setMedicine("TynolX","ABC IncX.","One per dayX");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine/1")
            .request()
            .put(Entity.json(updatedMed));

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 27. Delete medicine as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test27_deleteMedicine_user_forbidden() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/2")
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    /**
     * 28. Delete medicine as ADMIN_ROLE (200 OK)
     */
    @Transactional
    @Test
    public void test28_deleteMedicine_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine/2")
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 29. Get all medical schools with ADMIN_ROLE (200 OK)
     */
    @Test
    public void test09_getAllMedicalSchools_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 30. Get all medical schools with User_ROLE (200 OK)
     */
    @Test
    public void test30_getAllMedicalSchools_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 31. Get specific medical school by id with ADMIN_ROLE (200 OK)
     */
    @Test
    public void test31_getMedicalSchoolById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/2")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 32. Get specific medical school by id with User_ROLE (200 OK)
     */
    @Test
    public void test32_getMedicalSchoolById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/2")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 33. Create medical school as user_ROLE (403)
     */
    @Test
    public void test33_createMedicalSchool_user_fail() {
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
     * 34. Create medical school as user_ROLE (403 forbidden)
     */
    @Test
    public void test34_createMedicalSchool_user_fail() {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("CST Medical CollegeX");

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(403));
    }


    
    /**
     * 35. Update medical school as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test35_updateMedicalSchool_admin_success() {
        PublicSchool update = new PublicSchool();
        update.setId(2);
        update.setName("Updated Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/2")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 36. Update medical school as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test15_updateMedicalSchool_user_forbidden() {
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
     * 37. Delete medical school as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test48_deleteMedicalSchool_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/2")
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }
    
    
    /**
     * 38. Delete medical school as ADMIN_ROLE (200 OK)
     */
    @Transactional
    @Test
    public void test38_deleteMedicalSchool_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/1")
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    
    /**
     * 39. Get all medical training as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test39_getAllMedicalTrainings_asAdmin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicaltraining")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 40. Get all medical training as user_ROLE (200 OK)
     */
    @Test
    public void test40_getAllMedicalTrainings_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 41. Get specific medical training by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test41_getMedicalTrainingById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicaltraining/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 42. Get specific medical training by ID as User_ROLE (200 OK)
     */
    @Test
    public void test42_getMedicalTrainingById_user_success() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * 43. Create medical school as User_ROLE (403 forbidden)
     */
    @Test
    public void test43_createMedicalTraning_user_fail() {
    	
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
            .register(userAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newTraning, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(403));
    }
   
    /**
     * 44. Update medical training as User_ROLE (403 forbidden)
     */
    @Test
    public void test44_updateMedicalTraining_user_fail() {
    
    	MedicalTraining existing = new MedicalTraining();
    	
    	DurationAndStatus durationAndStatus = new DurationAndStatus();
        durationAndStatus.setStartDate(LocalDateTime.now().minusDays(1)); 
        durationAndStatus.setEndDate(LocalDateTime.now()); 
        durationAndStatus.setActive((byte) 0); 
        existing.setDurationAndStatus(durationAndStatus);
        
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/1")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(existing, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * 45. Delete medical training as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test45_deleteMedicalTraining_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/1") 
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }

    /**
     * 46. Get all prescriptions as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test46_getAllPrescriptions_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("prescription")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 47. Get prescription by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test28_getPrescriptionById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("prescription/1/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 48. create new prescription by user role (403 Forbidden)
     */
    @Test
    public void test48_createPrescription_user_fail() {

        Prescription newPrescription = new Prescription();
        PrescriptionPK prescriptionPK = new PrescriptionPK(2,1);

        newPrescription.setId(prescriptionPK);

        newPrescription.setNumberOfRefills(3);
        newPrescription.setPrescriptionInformation("Take with food");

        Response response = webTarget
            .register(userAuth)
            .path("prescription")  
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPrescription, MediaType.APPLICATION_JSON));

        // Check the response status
        assertThat(response.getStatus(), is(403));  
    }

    /**
     * 49. Delete prescription as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test49_deleteMedicalTraining_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription/1/1") 
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }

    /**
     * 50. Get all medical certificates as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test50_getAllMedicalCertificates_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * 51. Get medical certificate by ID as ADMIN_ROLE (200 OK)
     */
    @Test
    public void test51_getMedicalCertificateById_admin_success() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
    }


    /**
     * 52. Add medical certificate as USER_ROLE (403 Forbidden)
     */
    @Test
    public void test52_createMedicalCertificate_user_fail() {
        Map<String, Integer> requestBody = Map.of("Physician_id", 2);

        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request()
            .post(Entity.json(requestBody));

        assertThat(response.getStatus(), is(403));
    }
    
//    /**
//     * 53. Add medical certificate as Admin_ROLE (200)
//     */
//    @Test
//    public void test53_createMedicalCertificate_admin_success() {
//        Map<String, Integer> requestBody = Map.of("Physician_id", 2);
//
//        Response response = webTarget
//            .register(userAuth)
//            .path("medicalcertificate")
//            .request()
//            .post(Entity.json(requestBody));
//
//        assertThat(response.getStatus(), is(200));
//    }
//    
//    /**
//     * 54. Update medical certificate as admin_ROLE 
//     */
//    @Test
//    public void test54_updateMedicalCertificate_admin_success() {
//        MedicalCertificate updated = new MedicalCertificate();
//        updated.setSigned((byte) 0);
//        
//        Response response = webTarget
//            .register(userAuth)
//            .path("medicalcertificate")
//            .request(MediaType.APPLICATION_JSON)
//            .put(Entity.entity(updated, MediaType.APPLICATION_JSON));
//
//        assertThat(response.getStatus(), is(200));
//    }
   

    /**
     * 53. Delete medical certificate as USER_ROLE (403 Forbidden)
     */
    @Transactional
    @Test
    public void test53_deleteMedicalCertificate_user_fail() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/2") 
            .request()
            .delete();

        assertThat(response.getStatus(), anyOf(is(401), is(403)));
    }

}