package acmemedical.rest.resource;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Patient;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import static acmemedical.utility.MyConstants.*;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path(PATIENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    protected ACMEMedicalService service;

    // Get all patients
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getAllPatients() {
        LOG.debug("Retrieving all patients...");
        List<Patient> patients = service.getAllPatients();
        LOG.debug("Patients found = {}", patients);
        return Response.ok(patients).build();
    }

    // Get patient by id
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{patientId}")
    public Response getPatientById(@PathParam("patientId") int patientId) {
        LOG.debug("Retrieving patient with id = {}", patientId);
        Patient patient = service.getPatientById(patientId);
        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }
        return Response.ok(patient).build();
    }

    // Create new patient
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPatient(Patient newPatient) {
        LOG.debug("Adding a new patient = {}", newPatient);
        if (service.isPatientDuplicated(newPatient)) {
            return Response.status(Response.Status.CONFLICT).entity("Patient already exists").build();
        }
        Patient createdPatient = service.persistPatient(newPatient);
        return Response.ok(createdPatient).build();
    }

    // Update patient by id
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{patientId}")
    public Response updatePatient(@PathParam("patientId") int patientId, Patient updatingPatient) {
        LOG.debug("Updating patient with id = {}", patientId);
        Patient updatedPatient = service.updatePatientById(patientId, updatingPatient);
        if (updatedPatient == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Patient not found").build();
        }
        return Response.ok(updatedPatient).build();
    }

    // Delete patient by id
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{patientId}")
    public Response deletePatient(@PathParam("patientId") int patientId) {
        LOG.debug("Deleting patient with id = {}", patientId);
        service.deletePatientById(patientId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
