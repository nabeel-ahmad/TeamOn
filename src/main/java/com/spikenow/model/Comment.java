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
@JsonIgnoreProperties({"id", "activity"})
public class Comment {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User sender;
	
	@ManyToOne
	private Activity activity;
	
	private Date sentOn;
	
	private String text;
	
	
	public Comment() {
		super();
	}

	public Comment(User sender, Activity activity, String text, Date sentOn) {
		super();
		this.sender = sender;
		this.activity = activity;
		this.text = text;
		this.sentOn = sentOn;
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

	public Date getSentOn() {
		return sentOn;
	}

	public void setSentOn(Date sentOn) {
		this.sentOn = sentOn;
	}

}