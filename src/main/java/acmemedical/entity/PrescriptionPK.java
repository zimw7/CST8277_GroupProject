/********************************************************************************************************
 * File:  PrescriptionPK.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@SuppressWarnings("unused")
/**
 * The primary key class for the prescription database table.
 */
//TODO PRPK01 - What annotation is used to define an object which can be embedded in other entities?
//Hint - @Access is used to establish where the annotation for JPA will be placed, field or properties. 
@Embeddable
@Access(AccessType.FIELD)
public class PrescriptionPK implements Serializable {
	// Default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Basic(optional = false)
	@Column(name = "physician_id", nullable = false)
	private int physicianId;

	@Basic(optional = false)
	@Column(name = "patient_id", nullable = false)
	private int patientId;

	public PrescriptionPK() {
	}

	public PrescriptionPK(int physicianId, int patientId) {
		setPhysicianId(physicianId);
		setPatientId(patientId);
	}

	public int getPhysicianId() {
		return this.physicianId;
	}

	public void setPhysicianId(int physicianId) {
		this.physicianId = physicianId;
	}

	public int getPatientId() {
		return this.patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc.  change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		return prime * result + Objects.hash(getPhysicianId(), getPatientId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof PrescriptionPK otherPrescriptionPK) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getPhysicianId(), otherPrescriptionPK.getPhysicianId()) &&
				Objects.equals(this.getPatientId(),  otherPrescriptionPK.getPatientId());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PrescriptionPK [physicianId = ");
		builder.append(physicianId);
		builder.append(", patientId = ");
		builder.append(patientId);
		builder.append("]");
		return builder.toString();
	}

}
