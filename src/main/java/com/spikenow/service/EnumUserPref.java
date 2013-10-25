package com.spikenow.service;

import java.util.ArrayList;
import java.util.List;

public enum EnumUserPref {
	
	joinRequest_sent_withdrawn(true, "I send a join request or withdraw"),
	joinRequest_received_withdrawn(false, "I recieve a join request or someone withdraws"),
	joinRequest_responded(false, "I am accepted or declined"),
	myActivity_created_updated(true, "I create or update a game"),
	myActivity_canceled(true, "I cancel a game"),
	joinedActivity_canceled(false, "My joined game is updated or cancelled"),
	cohost_invited(true, "I am made co-host of a game"),
	playerlist_invited(false, "I am invited through a plyer list"),
	message_revieved(true, "Game organizer sends a message"),
	
	fb_publish(false, "Post games on facebook"),
	fb_publish_on(false, "Post on facebook wall");
	
	private Boolean pushDisabled;
	private String label;
	
	private EnumUserPref(Boolean pushDisabled, String label) {
		this.setPushDisabled(pushDisabled);
		this.label = label;
		
	}

	public static List<String> getAll() {
		List<String> list = new ArrayList<String>();
		for (EnumUserPref p : values()) {
			list.add(p.toString());
		}
		return list;
	}

	public void setPushDisabled(Boolean pushDisabled) {
		this.pushDisabled = pushDisabled;
	}

	public Boolean getPushDisabled() {
		return pushDisabled;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
