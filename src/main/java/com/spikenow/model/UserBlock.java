package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class UserBlock {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User blockedUser;

	@ManyToOne
	private User blockedBy;
	
	public UserBlock() {
		super();
	}

	public UserBlock(User blockedUser, User blockedBy) {
		super();
		this.blockedUser = blockedUser;
		this.blockedBy = blockedBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getBlockedUser() {
		return blockedUser;
	}

	public void setBlockedUser(User blockedUser) {
		this.blockedUser = blockedUser;
	}

	public User getBlockedBy() {
		return blockedBy;
	}

	public void setBlockedBy(User blockedBy) {
		this.blockedBy = blockedBy;
	}
	
}