package com.spikenow.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumUserPref {
	
	joinRequest_sent_withdrawn(true, "I send a join request or withdraw"),
	joinRequest_received_withdrawn(false, "I receive a join request or someone withdraws"),
	joinRequest_responded(false, "I am accepted or declined"),
	myActivity_created_updated(true, "I create or update a game"),
	myActivity_canceled(true, "I cancel a game"),
	joinedActivity_canceled(false, "My joined game is updated or cancelled"),
	cohost_invited(true, "I am made co-host of a game"),
	playerlist_invited(false, "I am invited through a player list"),
	message_revieved(true, "Game organizer sends a message"),
	daily_digest(true, "Upcoming and recent games summary"),
	added_to_playerlist(false, "Someone adds me to a player list"),
	new_direct_message(false, "I receive a new direct message"), 
	
	fb_publish(false, "Post games on facebook", "true"),
	fb_publish_on(false, "Post on facebook wall", "me"),
	fb_publish_on_timeline_organize(false, "Add games organized to my facebook timeline", "true"),
	fb_publish_on_timeline_play(false, "Add games played to my facebook timeline", "true"); 
	
	private Boolean pushDisabled;
	private String label;
	private String value = "";
	
	private EnumUserPref(Boolean pushDisabled, String label) {
		this.setPushDisabled(pushDisabled);
		this.label = label;
	}

	private EnumUserPref(Boolean pushDisabled, String label, String value) {
		this.setPushDisabled(pushDisabled);
		this.label = label;
		this.value = value;
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

	public static EnumUserPref getByName(String userPref) {
		for (EnumUserPref p : values()) {
			if(p.toString().equals(userPref))
				return p;
		}
		return null;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
