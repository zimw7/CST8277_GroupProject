/********************************************************************************************************
 * File:  Patient.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@SuppressWarnings("unused")

/**
 * The persistent class for the patient database table.
 */
//TODO PA01 - Add the missing annotations.
//TODO PA02 - Do we need a mapped super class?  If so, which one?
@Entity
@Table(name = "patient")
@Access(AccessType.FIELD)
@NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p")
@NamedQuery(name = "Patient.findById", query = "SELECT p FROM Patient p WHERE p.id = :param1")
public class Patient extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// TODO PA03 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	// TODO PA04 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	// TODO PA05 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "year_of_birth", nullable = false)
	private int year;

	// TODO PA06 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "home_address", nullable = false, length = 100)
	private String address;

	// TODO PA07 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "height_cm", nullable = false)
	private int height;

	// TODO PA08 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "weight_kg", nullable = false)
	private int weight;

	// TODO PA09 - Add missing annotations.
	@Basic(optional = false)
	@Column(name = "smoker", nullable = false)
	private byte smoker;

	// TODO PA10 - Add annotations for 1:M relation.  What should be the cascade and fetch types?
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "patient")
	private Set<Prescription> prescriptions = new HashSet<>();

	public Patient() {
		super();
	}

	public Patient(String firstName, String lastName, int year, String address, int height, int weight, byte smoker) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
		this.year = year;
		this.address = address;
		this.height = height;
		this.weight = weight;
		this.smoker = smoker;
	}

	public Patient setPatient(String firstName, String lastName, int year, String address, int height, int weight, byte smoker) {
		setFirstName(firstName);
		setLastName(lastName);
		setYear(year);
		setAddress(address);
		setHeight(height);
		setWeight(weight);
		setSmoker(smoker);
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public byte getSmoker() {
		return smoker;
	}

	public void setSmoker(byte smoker) {
		this.smoker = smoker;
	}
	
	public Set<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescription(Set<Prescription> prescriptions) {
		this.prescriptions = prescriptions;
	}

	//Inherited hashCode/equals is sufficient for this Entity class

}
