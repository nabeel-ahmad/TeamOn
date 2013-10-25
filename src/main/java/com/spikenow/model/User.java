package com.spikenow.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.spikenow.util.PropertiesUtil;

 
/**
 * @author Nabeel
 *
 */
@XmlRootElement(name = "user")
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"password","activationCode","access_token"})
public class User {
	
	@Id
	private String id;
	private String authProvider;
	private String name;
	private String first_name;
	private String last_name;
	private String gender;
	private String link;
	private String picture;
	private String email;
	private Float timezone;
	private String password;
	private String activationCode;
	private String access_token;
	private Boolean firstLogin = true;
	
	private Float latitude;
	private Float longitude;
	private String address;
	
	@Transient
	private Integer organizedGameCount = 0;
	@Transient
	private Integer joinedGameCount = 0;
	@Transient
	private List<PlayerList> inMyPlayerLists;
	
	public User() {
 
	}
 
	public User(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		if(name == null && first_name != null) {
			name = first_name;
			if(last_name != null)
				name = first_name + " " + last_name;
		}
		return name == null ? "" : name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setAuthProvider(String authProvider) {
		this.authProvider = authProvider;
	}

	public String getAuthProvider() {
		return authProvider;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	@JsonIgnore
	public void setPicture(String picture) {
		this.picture = picture;
	}

	@SuppressWarnings("rawtypes")
	public void setPicture(Map picture) throws IOException {
		Map pic = (Map) picture.get("data");
		if (pic.get("url") != null && !pic.get("url").equals("")) {
			this.picture = pic.get("url").toString();
		} else {
			this.picture = PropertiesUtil.getPath("misc_images") + "fb_sil.gif";
		}
	}

	public String getPicture() throws IOException {
		if(picture !=null) return picture;
		
		return PropertiesUtil.getPath("misc_images") + "person@2x.png";
	}

	public String getFirst_name() {
		if (first_name != null) {
			return first_name;
		} else if(name != null && name.trim().length() > 0) {
			return new StringTokenizer(name).nextToken();
		}
		return null;
	}

	public void setFirst_name(String firstName) {
		first_name = firstName;
	}

	public void setGiven_name(String firstName) {
		first_name = firstName;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String lastName) {
		last_name = lastName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setProfile_image_url(String profileImageUrl) {
		picture = profileImageUrl;
	}

	public void setId_str(String idStr) {
		id = idStr;
	}

	public Float getTimezone() {
		return timezone;
	}

	public void setTimezone(Float timezone) {
		this.timezone = timezone;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public Float getLongitude() {
		return longitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public Integer getOrganizedGameCount() {
		return organizedGameCount;
	}

	public void setOrganizedGameCount(Integer organizedGameCount) {
		this.organizedGameCount = organizedGameCount;
	}

	public Integer getJoinedGameCount() {
		return joinedGameCount;
	}

	public void setJoinedGameCount(Integer joinedGameCount) {
		this.joinedGameCount = joinedGameCount;
	}

	public List<PlayerList> getInMyPlayerLists() {
		return inMyPlayerLists;
	}

	public void setInMyPlayerLists(List<PlayerList> inMyPlayerLists) {
		this.inMyPlayerLists = inMyPlayerLists;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(Boolean firstLogin) {
		this.firstLogin = firstLogin;
	}
	
	public boolean isActive() {
		return this.access_token != null;
	}

}