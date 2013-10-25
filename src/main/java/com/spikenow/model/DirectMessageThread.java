package com.spikenow.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class DirectMessageThread {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User sender;

	@ManyToOne
	private User recipient;
	
	private Date updatedOn;
	
	@Transient
	private List<DirectMessage> messages; 
	
	public DirectMessageThread() {
		super();
	}

	public DirectMessageThread(User sender, User recipient) {
		super();
		this.sender = sender;
		this.recipient = recipient;
	}

	public DirectMessageThread(Long id) {
		super();
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public List<DirectMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<DirectMessage> messages) {
		this.messages = messages;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

}