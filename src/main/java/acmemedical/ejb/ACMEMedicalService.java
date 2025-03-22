/********************************************************************************************************
 * File:  ACMEMedicalService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 * Group Members:
 * - Jiaying Qiu
 * - Tianshu Liu
 * - Mengying Liu
 * - Zimeng Wang
 * 
 */
package acmemedical.ejb;

import static acmemedical.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmemedical.utility.MyConstants.PARAM1;
import static acmemedical.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmemedical.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmemedical.utility.MyConstants.PU_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.entity.Physician.ALL_PHYSICIANS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.IS_DUPLICATE_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Patient;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.SecurityRole;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;
import acmemedical.entity.MedicalSchool;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMEMedicalService
 */
@Singleton
public class ACMEMedicalService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Physician> getAllPhysicians() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Physician> cq = cb.createQuery(Physician.class);
        cq.select(cq.from(Physician.class));
        return em.createQuery(cq).getResultList();
    }

    public Physician getPhysicianById(int id) {
        return em.find(Physician.class, id);
    }

    @Transactional
    public Physician persistPhysician(Physician newPhysician) {
        em.persist(newPhysician);
        return newPhysician;
    }

    @Transactional
    public void buildUserForNewPhysician(Physician newPhysician) {
        SecurityUser userForNewPhysician = new SecurityUser();
        userForNewPhysician.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPhysician.getFirstName() + "." + newPhysician.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPhysician.setPwHash(pwHash);
        userForNewPhysician.setPhysician(newPhysician);
        
        TypedQuery<SecurityRole> rolesQuery = em.createNamedQuery(SecurityRole.FIND_BY_ROLE_NAME, SecurityRole.class);
        rolesQuery.setParameter(PARAM1, USER_ROLE);
        /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
        SecurityRole userRole = rolesQuery.getSingleResult();
        
        userForNewPhysician.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPhysician);
        em.persist(userForNewPhysician);
    }

    @Transactional
    public Medicine setMedicineForPhysicianPatient(int physicianId, int patientId, Medicine newMedicine) {
        Physician physicianToBeUpdated = em.find(Physician.class, physicianId);
        if (physicianToBeUpdated != null) { // Physician exists
            Set<Prescription> prescriptions = physicianToBeUpdated.getPrescriptions();
            prescriptions.forEach(p -> {
                if (p.getPatient().getId() == patientId) {
                    if (p.getMedicine() != null) { // Medicine exists
                        Medicine medicine = em.find(Medicine.class, p.getMedicine().getId());
                        medicine.setMedicine(newMedicine.getDrugName(),
                        				  newMedicine.getManufacturerName(),
                        				  newMedicine.getDosageInformation());
                        em.merge(medicine);
                    }
                    else { // Medicine does not exist
                        p.setMedicine(newMedicine);
                        em.merge(physicianToBeUpdated);
                    }
                }
            });
            return newMedicine;
        }
        else return null;  // Physician doesn't exists
    }

    /**
     * To update a physician
     * 
     * @param id - id of entity to update
     * @param physicianWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Physician updatePhysicianById(int id, Physician physicianWithUpdates) {
    	Physician physicianToBeUpdated = getPhysicianById(id);
        if (physicianToBeUpdated != null) {
            em.refresh(physicianToBeUpdated);
            em.merge(physicianWithUpdates);
            em.flush();
        }
        return physicianToBeUpdated;
    }

    /**
     * To delete a physician by id
     * 
     * @param id - physician id to delete
     */
    @Transactional
    public void deletePhysicianById(int id) {
        Physician physician = getPhysicianById(id);
        if (physician != null) {
            em.refresh(physician);
            TypedQuery<SecurityUser> findUser = 
                    /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
                       so that when we remove it, the relationship from SECURITY_USER table
                       is not dangling
                    */ 
                    em.createNamedQuery(SecurityUser.USER_BY_PHYSICIAN_ID, SecurityUser.class);
                    findUser.setParameter("physicianId", id);

            SecurityUser sUser = findUser.getSingleResult();
            if (sUser != null) {
                em.refresh(sUser); 
                em.remove(sUser);
            }
            em.refresh(physician);
            em.remove(physician);
        }
    }
    
    public List<MedicalSchool> getAllMedicalSchools() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalSchool> cq = cb.createQuery(MedicalSchool.class);
        cq.select(cq.from(MedicalSchool.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public MedicalSchool getMedicalSchoolById(int id) {
        TypedQuery<MedicalSchool> specificMedicalSchoolQuery = em.createNamedQuery(SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, MedicalSchool.class);
        specificMedicalSchoolQuery.setParameter(PARAM1, id);
        return specificMedicalSchoolQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public MedicalSchool deleteMedicalSchool(int id) {
        //MedicalSchool ms = getMedicalSchoolById(id);
    	MedicalSchool ms = getById(MedicalSchool.class, MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, id);
        if (ms != null) {
            Set<MedicalTraining> medicalTrainings = ms.getMedicalTrainings();
            List<MedicalTraining> list = new LinkedList<>();
            medicalTrainings.forEach(list::add);
            list.forEach(mt -> {
                if (mt.getCertificate() != null) {
                    MedicalCertificate mc = getById(MedicalCertificate.class, MedicalCertificate.ID_CARD_QUERY_NAME, mt.getCertificate().getId());
                    mc.setMedicalTraining(null);
                }
                mt.setCertificate(null);
                em.merge(mt);
            });
            em.remove(ms);
            return ms;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(MedicalSchool newMedicalSchool) {
        TypedQuery<Long> allMedicalSchoolsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allMedicalSchoolsQuery.setParameter(PARAM1, newMedicalSchool.getName());
        return (allMedicalSchoolsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public MedicalSchool persistMedicalSchool(MedicalSchool newMedicalSchool) {
        em.persist(newMedicalSchool);
        return newMedicalSchool;
    }

    @Transactional
    public MedicalSchool updateMedicalSchool(int id, MedicalSchool updatingMedicalSchool) {
    	MedicalSchool medicalSchoolToBeUpdated = getMedicalSchoolById(id);
        if (medicalSchoolToBeUpdated != null) {
            em.refresh(medicalSchoolToBeUpdated);
            medicalSchoolToBeUpdated.setName(updatingMedicalSchool.getName());
            em.merge(medicalSchoolToBeUpdated);
            em.flush();
        }
        return medicalSchoolToBeUpdated;
    }
    
    @Transactional
    public MedicalTraining persistMedicalTraining(MedicalTraining newMedicalTraining) {
        em.persist(newMedicalTraining);
        return newMedicalTraining;
    }
    
    public List<MedicalTraining> getAllMedicalTrainings() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalTraining> cq = cb.createQuery(MedicalTraining.class);
        cq.select(cq.from(MedicalTraining.class));
        return em.createQuery(cq).getResultList();
    }
    
    public MedicalTraining getMedicalTrainingById(int mtId) {
        TypedQuery<MedicalTraining> allMedicalTrainingQuery = em.createNamedQuery(MedicalTraining.FIND_BY_ID, MedicalTraining.class);
        allMedicalTrainingQuery.setParameter(PARAM1, mtId);
        return allMedicalTrainingQuery.getSingleResult();
    }
    
    @Transactional
    public void deleteMedicalTraining(int id) {

        MedicalTraining newMedicalTraining= getById(MedicalTraining.class, MedicalTraining.FIND_BY_ID, id);
        if (newMedicalTraining!= null) {
            em.remove(newMedicalTraining);
        }
    }


    @Transactional
    public MedicalTraining updateMedicalTraining(int id, MedicalTraining medicalTrainingWithUpdates) {
    	MedicalTraining medicalTrainingToBeUpdated = getMedicalTrainingById(id);
        if (medicalTrainingToBeUpdated != null) {
            em.refresh(medicalTrainingToBeUpdated);
            em.merge(medicalTrainingWithUpdates);
            em.flush();
        }
        return medicalTrainingToBeUpdated;
    }
    
    /*public boolean isMedicalTrainingDuplicated(MedicalTraining newMedicalTraining) {
        TypedQuery<Long> allMedicalTrainingQuery = em.createNamedQuery(MedicalTraining.IS_DUPLICATE_QUERY, Long.class);
        allMedicalTrainingQuery.setParameter(PARAM1, newMedicalTraining.getName());
        return (allMedicalTrainingQuery.getSingleResult() >= 1);
    }
*/
    // Get all patients
    public List<Patient> getAllPatients() {
        TypedQuery<Patient> query = em.createQuery("SELECT p FROM Patient p", Patient.class);
        return query.getResultList();
    }

    // Get patient by id
    public Patient getPatientById(int patientId) {
        return em.find(Patient.class, patientId);
    }

    // Add new patient
    @Transactional
    public Patient persistPatient(Patient newPatient) {
        em.persist(newPatient);
        return newPatient;
    }

    // Check if the patient already exists
    public boolean isPatientDuplicated(Patient newPatient) {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.firstName = :firstName AND p.lastName = :lastName", Long.class);
        query.setParameter("firstName", newPatient.getFirstName());
        query.setParameter("lastName", newPatient.getLastName());
        return query.getSingleResult() > 0;
    }

    // Update patient by id
    @Transactional
    public Patient updatePatientById(int id, Patient patientWithUpdates) {
        Patient patientToBeUpdated = getPatientById(id);
        if (patientToBeUpdated != null) {
            patientToBeUpdated.setFirstName(patientWithUpdates.getFirstName());
            patientToBeUpdated.setLastName(patientWithUpdates.getLastName());
            patientToBeUpdated.setYear(patientWithUpdates.getYear());
            patientToBeUpdated.setAddress(patientWithUpdates.getAddress());
            patientToBeUpdated.setHeight(patientWithUpdates.getHeight());
            patientToBeUpdated.setWeight(patientWithUpdates.getWeight());
            patientToBeUpdated.setSmoker(patientWithUpdates.getSmoker());
            em.merge(patientToBeUpdated);
        }
        return patientToBeUpdated;
    }

    // Delete patient by id
    @Transactional
    public void deletePatientById(int id) {
        Patient patient = getPatientById(id);
        if (patient != null) {
            em.remove(patient);
        }
    }
    
    public List<Medicine> getAllMedicines() {
        TypedQuery<Medicine> query = em.createNamedQuery("Medicine.findAll", Medicine.class);
        return query.getResultList();
    }

    public Medicine getMedicineById(int id) {
        return em.find(Medicine.class, id);
    }

    public Medicine persistMedicine(Medicine newMedicine) {
        em.persist(newMedicine);
        return newMedicine;
    }

    public boolean isMedicineDuplicated(Medicine newMedicine) {
    	try {
            // Use the named query to check if a medicine with the same drug name already exists
            Query query = em.createNamedQuery("Medicine.isDuplicate");
            query.setParameter("drugName", newMedicine.getDrugName());
            List<?> result = query.getResultList();
            return !result.isEmpty(); // Return true if a medicine with the same drug name exists
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception
            return false;
        }
    }

    public Medicine updateMedicine(int id, Medicine updatedMedicine) {
        Medicine existingMedicine = em.find(Medicine.class, id);
        if (existingMedicine != null) {
            existingMedicine.setDrugName(updatedMedicine.getDrugName());
            existingMedicine.setManufacturerName(updatedMedicine.getManufacturerName());
            existingMedicine.setDosageInformation(updatedMedicine.getDosageInformation());
            em.merge(existingMedicine);
            return existingMedicine;
        }
        return null;
    }

    public Medicine deleteMedicine(int id) {
        Medicine medicine = em.find(Medicine.class, id);
        if (medicine.getPrescriptions() != null && !medicine.getPrescriptions().isEmpty()) {
            // Handle prescriptions before deletion if necessary
            medicine.getPrescriptions().clear();  // or some other clean-up operation
        }
        if (medicine != null) {
            em.remove(medicine);
            return medicine;
        }
        return null;
    }
   
}