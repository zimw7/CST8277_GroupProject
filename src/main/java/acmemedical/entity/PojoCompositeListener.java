/********************************************************************************************************
 * File:  PojoCompositeListener.java Course Materials CST 8277
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
package acmemedical.entity;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@SuppressWarnings("unused")

public class PojoCompositeListener {

	// TODO PCL01 - What annotation is used when we want to do something just before object is INSERT'd into database?
	@PrePersist
	public void setCreatedOnDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		LocalDateTime now = LocalDateTime.now();
		// TODO PCL02 - What member field(s) do we wish to alter just before object is INSERT'd in the database?
		pojoBaseComposite.setCreated(now);
	}

	// TODO PCL03 - What annotation is used when we want to do something just before object is UPDATE'd into database?
	@PreUpdate
	public void setUpdatedDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		// TODO PCL04 - What member field(s) do we wish to alter just before object is UPDATE'd in the database?
		pojoBaseComposite.setUpdated(LocalDateTime.now());
	}

}
