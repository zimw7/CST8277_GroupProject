/********************************************************************************************************
 * File:  MedicalTrainingResource.java Course Materials CST 8277
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
package acmemedical.rest.resource;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import jakarta.ws.rs.core.Response.Status;
import static acmemedical.utility.MyConstants.MEDICAL_SCHOOL_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Physician;
import acmemedical.entity.SecurityUser;


@Path("/medicaltraining")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalTrainingResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    //unsure if we really need this for courses
    @Inject
    protected SecurityContext sc;


    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMedicalTrainings() {
        LOG.debug("retrieving all medical training ...");
        List<MedicalTraining> students = service.getAllMedicalTrainings();
        Response response = Response.ok(students).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicalTrainingById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific c " + id);
        Response response = null;
        Physician physician = null;

        MedicalTraining MedicalTraining = service.getMedicalTrainingById(id);

        if (sc.isCallerInRole(ADMIN_ROLE)) {

            response = Response.status(MedicalTraining == null ? Status.NOT_FOUND : Status.OK).entity(MedicalTraining).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            physician = sUser.getPhysician();
            if (physician != null && MedicalTraining != null && physician.equals(MedicalTraining.getCertificate().getOwner())) {
                response = Response.status(Status.OK).entity(physician).build();
            } else {
                throw new ForbiddenException("User trying to access resource it does not own (wrong userid)");
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalTraining (MedicalTraining newTraining) {

        LOG.debug("Adding new medical training", newTraining.getId());
        Response response = null;
        MedicalTraining newMedicalTraining = service.persistMedicalTraining(newTraining);

        response = Response.ok(newMedicalTraining).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicalTrainingById(int id) {
        LOG.debug("Deleting medical training with id = {}", id);
        service.deleteMedicalTraining(id);
        return Response.ok(id).build();
    }
    
}

