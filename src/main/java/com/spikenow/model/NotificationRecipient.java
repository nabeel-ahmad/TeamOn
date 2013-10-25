package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class NotificationRecipient {
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private Notification notification;
	
	@ManyToOne
	private User recipient;
	
	private Boolean unread = true;

	public NotificationRecipient() {
		super();
	}
	
	public NotificationRecipient(Notification notification, User recipient) {
		this.notification = notification;
		this.recipient = recipient;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public void setUnread(Boolean unread) {
		this.unread = unread;
	}

	public Boolean getUnread() {
		return unread;
	}
}