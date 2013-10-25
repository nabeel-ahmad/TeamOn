package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * 
 * @author Nabeel
 *
 */
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Subscription {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	@ManyToOne
	private ActivityType activityType;
	@ManyToOne
	private User user;
	private String latitude;
	private String longitude;
	private Float maxDistance;
	
	private boolean email = true;
	private boolean push = true;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ActivityType getActivityType() {
		return activityType;
	}
	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public Float getMaxDistance() {
		return maxDistance;
	}
	public void setMaxDistance(Float maxDistance) {
		this.maxDistance = maxDistance;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean isEmail() {
		return email;
	}
	public void setEmail(boolean email) {
		this.email = email;
	}
	public boolean isPush() {
		return push;
	}
	public void setPush(boolean push) {
		this.push = push;
	}

}
