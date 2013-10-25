package com.spikenow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"id", "thread"})
public class DirectMessage {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User sender;
	
	@ManyToOne
	private DirectMessageThread thread;
	
	private Date sentOn;
	private String text;
	
	@ManyToOne
	private Activity activity;


	public DirectMessage() {
		super();
	}

	public DirectMessage(User sender, Date sentOn, String text, Activity activity) {
		super();
		this.sender = sender;
		this.sentOn = sentOn;
		this.text = text;
		this.activity = activity;
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

	public DirectMessageThread getThread() {
		return thread;
	}

	public void setThread(DirectMessageThread thread) {
		this.thread = thread;
	}

	public Date getSentOn() {
		return sentOn;
	}

	public void setSentOn(Date sentOn) {
		this.sentOn = sentOn;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

}