/********************************************************************************************************
 * File:  MedicalCertificateResource.java Course Materials CST 8277
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

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Physician;
import acmemedical.entity.SecurityUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import static acmemedical.utility.MyConstants.*;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

@Path("medicalcertificate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalCertificateResource {
	
	private static final Logger LOG = LogManager.getLogger();

    @Inject
    protected ACMEMedicalService service;
    
    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getAllMedicalCertificates() {
        LOG.debug("Retrieving all medical certificates...");
        List<MedicalCertificate> MedicalCertificates = service.getAllCertificates();
        LOG.debug("Medical certificates found = {}", MedicalCertificates);
        return Response.ok(MedicalCertificates).build();
    }
    
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicalCertificateById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int certificateId) {
        LOG.debug("Retrieving medical certificate with id = {}", certificateId);
       
        Response response = null;        
        MedicalCertificate certificate = service.getCertificateById(certificateId);
        
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            certificate = service.getCertificateById(certificateId);
            response = Response.status(certificate == null ? Status.NOT_FOUND : Status.OK).entity(certificate).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Physician Physician = sUser.getPhysician();
            certificate = service.getCertificateById(certificateId);
            if (Physician != null && Physician.getId() == certificate.getOwner().getId()) {
                response = Response.status(Status.OK).entity(certificate).build();
            } else {
                throw new ForbiddenException("User trys to access resource, it does not own (wrong userid)");
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalCertificate(Map<String, Integer> request) {
    	
    	try {
    		int PhysicianId = request.get("Physician_id").intValue();
    		Physician Physician = service.getPhysicianById(PhysicianId); 
    		if (Physician == null) {
    			return Response.status(Response.Status.NOT_FOUND)
		                        .entity("Physician is not found")
		                        .build();
    		}
    		
    		else {
    			MedicalCertificate newCertificate = new MedicalCertificate();
    			newCertificate.setOwner(Physician);
    			newCertificate = service.persistCertificate(newCertificate);
    			return Response.status(Status.OK).entity(newCertificate).build();
    		}
    	}
    	catch(Exception e) {
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Invalid Request. Internal error")
                    .build();
    	}
    }

    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicalCertificate(@PathParam(RESOURCE_PATH_ID_ELEMENT) int certificateId) {
        LOG.debug("Deleting medical certificate with id = {}", certificateId);
        Response response = null;
        service.deleteCertificateById(certificateId);
        response = Response.ok("Deleted Medical certificate with id: " + certificateId).build();
        return response;
        
    }


}