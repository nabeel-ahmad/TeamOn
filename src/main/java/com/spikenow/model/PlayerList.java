package com.spikenow.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.spikenow.util.PropertiesUtil;

@XmlRootElement(name = "playerList")
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"activityPlayerLists"})
public class PlayerList {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private User user;
	
	private String name;
	private String picture;
	
	//TODO: Fix Jackson views
	@OneToMany(mappedBy="playerList", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<Player> players;
	
	@OneToMany(mappedBy="playerList", cascade = CascadeType.ALL)
	private Set<ActivityPlayerList> activityPlayerLists;

	
	/**
	 * Write-only: list of IDs to add/remove users who can see the player list 
	 */
	@Transient
	private List<String> sharedWithUserIds; 

	/**
	 * Read-only: list of user to share the player list with
	 */
	//TODO: Fix Jackson views
	@OneToMany(mappedBy="playerList", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<UserPlayerList> sharedWithUsers; 
	
	/**
	 * Read-only: flag to identify if a player list is user's or shared by someone else
	 */
	@Transient
	private Boolean isMine;
	
	public PlayerList() {
		super();
	}
	
	
	public PlayerList(User user, String email) {
		this.user = user;
		this.name = email;
	}


	public PlayerList(Long id) {
		super();
		this.id = id;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}


	public void setPlayers(Set<Player> players) {
		this.players = players;
	}


	public Set<Player> getPlayers() {
		return players;
	}


	public List<String> getSharedWithUserIds() {
		return sharedWithUserIds;
	}


	public void setSharedWithUserIds(List<String> sharedWithUserIds) {
		this.sharedWithUserIds = sharedWithUserIds;
	}


	public Boolean getIsMine() {
		if(isMine == null) return true;
		return isMine;
	}


	public void setIsMine(Boolean isMine) {
		this.isMine = isMine;
	}


	public String getPicture() throws IOException {
		if(picture !=null) return picture;
		return PropertiesUtil.getPath("misc_images") + "person@2x.png";
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public List<UserPlayerList> getSharedWithUsers() {
		return sharedWithUsers;
	}

	public void setSharedWithUsers(List<UserPlayerList> sharedWithUsers) {
		this.sharedWithUsers = sharedWithUsers;
	}
	
}