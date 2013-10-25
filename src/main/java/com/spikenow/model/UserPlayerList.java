package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Player list sharing table
 * @author Nabeel
 *
 */
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"playerList"})
public class UserPlayerList {
	
	@Id
	@GeneratedValue
	private Long id;
	
	/**
	 * Player list is shared with this user 
	 */
	@ManyToOne
	private User user;
	
	@ManyToOne
	private PlayerList playerList;

	public UserPlayerList() {
		super();
	}

	public UserPlayerList(User user, PlayerList playerList) {
		super();
		this.user = user;
		this.playerList = playerList;
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

	public PlayerList getPlayerList() {
		return playerList;
	}

	public void setPlayerList(PlayerList playerList) {
		this.playerList = playerList;
	}
}