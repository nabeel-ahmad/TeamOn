package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.spikenow.enums.EnumUserPref;

@XmlRootElement(name = "userPreferences")
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"id","user"})
public class UserPreferences {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User user;
	
	private String code;
	
	@Transient
	private String label;
	
	private String value;
	
	private Boolean emailNotification;
	private Boolean pushNotification;
	
	@Transient
	private Boolean pushDisabled;
	
	
	public UserPreferences() {
		super();
	}
	
	public UserPreferences(String code, User user) {
		this.code = code;
		this.user = user;
	}
	
	public UserPreferences(User user, String name, String value, boolean pushNotification, boolean emailNotification) {
		this.user = user;
		this.code = name;
		this.value = value;
		this.pushNotification = pushNotification;
		this.emailNotification = emailNotification;
	}
	
	public UserPreferences(User user, EnumUserPref event, boolean pushNotification, boolean emailNotification) {
		this.user = user;
		this.code = event.toString();
		this.pushNotification = pushNotification;
		this.emailNotification = emailNotification;
		this.pushDisabled = event.getPushDisabled();
		this.label = event.getLabel();
		this.value = event.getValue();
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getEmailNotification() {
		return emailNotification;
	}

	public void setEmailNotification(Boolean emailNotification) {
		this.emailNotification = emailNotification;
	}

	public Boolean getPushNotification() {
		return pushNotification;
	}

	public void setPushNotification(Boolean pushNotification) {
		this.pushNotification = pushNotification;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPreferences other = (UserPreferences) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public void setPushDisabled(Boolean pushDisabled) {
		this.pushDisabled = pushDisabled;
	}

	public Boolean getPushDisabled() {
		return pushDisabled;
	}
}