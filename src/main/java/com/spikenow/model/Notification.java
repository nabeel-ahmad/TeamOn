package com.spikenow.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@XmlRootElement(name = "notification")
@Entity
@JsonIgnoreProperties(value = {"recipients"})
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Notification {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String text;
	private String type;
	private String data;
	private Long activity_id;
	private String icon;
	private Date createdOn = new Date();
	
	@Transient
	private Boolean unread = true;
	
	@OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
	private Set<NotificationRecipient> recipients;
	
	@ManyToOne
	private User sender;
	
	@ManyToOne
	private JoinRequest joinRequest; 

	public Notification() {
		super();
	}

	public Notification(String text, Boolean unread) {
		super();
		this.text = text;
		this.unread = unread;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setUnread(Boolean unread) {
		this.unread = unread;
	}

	public Boolean getUnread() {
		return unread;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setActivity_id(Long activity_id) {
		this.activity_id = activity_id;
	}

	public Long getActivity_id() {
		return activity_id;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getSender() {
		return sender;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	public void setJoinRequest(JoinRequest joinRequest) {
		this.joinRequest = joinRequest;
	}

	public JoinRequest getJoinRequest() {
		return joinRequest;
	}

	public void setRecipients(Set<NotificationRecipient> recipients) {
		this.recipients = recipients;
	}

	public Set<NotificationRecipient> getRecipients() {
		return recipients;
	}

}
