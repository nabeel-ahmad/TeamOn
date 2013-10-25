package com.spikenow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.spikenow.enums.EnumStatusTypes;

@XmlRootElement(name = "joinRequest")
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class JoinRequest {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private Activity activity;
	@ManyToOne
	private User user;
	
	private String status = EnumStatusTypes.NO_RESPONSE.toString();
	
	private Date createdOn;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

}
