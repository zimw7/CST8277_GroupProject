package acmemedical.rest.resource;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
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

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Medicine;
import static acmemedical.utility.MyConstants.ADMIN_ROLE;

@Path("/medicine")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicineResource {

    @EJB
    private ACMEMedicalService service;

    @GET
    public Response getAllMedicines() {
        List<Medicine> medicines = service.getAllMedicines();
        return Response.ok(medicines).build();
    }

    @GET
    @Path("/{medicineId}")
    public Response getMedicineById(@PathParam("medicineId") int medicineId) {
        Medicine medicine = service.getMedicineById(medicineId);
        if (medicine == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Medicine not found")
                           .build();
        }
        return Response.ok(medicine).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicine(Medicine newMedicine) {
        if (service.isMedicineDuplicated(newMedicine)) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("Medicine already exists")
                           .build();
        }
        Medicine addedMedicine = service.persistMedicine(newMedicine);
        return Response.status(Response.Status.CREATED).entity(addedMedicine).build();
    }

    @PUT
    @Path("/{medicineId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicine(@PathParam("medicineId") int medicineId, Medicine updatedMedicine) {
        Medicine medicine = service.updateMedicine(medicineId, updatedMedicine);
        if (medicine == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Medicine not found")
                           .build();
        }
        return Response.ok(medicine).build();
    }

    @DELETE
    @Path("/{medicineId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicine(@PathParam("medicineId") int medicineId) {
        Medicine deletedMedicine = service.deleteMedicine(medicineId);
        if (deletedMedicine == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Medicine not found")
                           .build();
        }
        return Response.ok(deletedMedicine).build();
    }
}

