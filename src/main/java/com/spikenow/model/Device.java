package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Device {
	
	@Id
	private String device_id;
	private String device_type;
	
	@Transient
	private int unreadCount;
	
	@ManyToOne
	private User user;

	public Device() {
		
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String deviceId) {
		device_id = deviceId;
	}

	public String getDevice_type() {
		return device_type;
	}

	public void setDevice_type(String deviceType) {
		device_type = deviceType;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}
	
}
