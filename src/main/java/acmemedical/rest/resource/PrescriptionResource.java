
package acmemedical.rest.resource;


import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.PHYSICIAN_PATIENT_MEDICINE_RESOURCE_PATH;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmemedical.utility.MyConstants.USER_ROLE;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;

@Path("/prescription")
@Consumes("application/json")
@Produces("application/json")
@RequestScoped
public class PrescriptionResource {

    @PersistenceContext(unitName = "acmemedical-PU")
    protected EntityManager em;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getAllPrescriptions() {
        List<Prescription> prescriptions = em.createNamedQuery("Prescription.findAll", Prescription.class).getResultList();
        return Response.ok(prescriptions).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{physicianId}/{patientId}")
    public Response getPrescriptionById(@PathParam("physicianId") int physicianId, @PathParam("patientId") int patientId) {
    	PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
    	Prescription prescription = em.find(Prescription.class, pk);
        if (prescription == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(prescription).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    @Transactional
    public Response addPrescription(Prescription newPrescription) {
        em.persist(newPrescription);
        return Response.ok(newPrescription).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{physicianId}/{patientId}")
    @Transactional
    public Response deletePrescription(@PathParam("physicianId") int physicianId, @PathParam("patientId") int patientId) {
    	PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
    	Prescription prescription = em.find(Prescription.class, pk);
        if (prescription == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.remove(prescription);
        return Response.noContent().build();
    }
}
