package com.spikenow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"id","activity"})
public class ActivityPlayerList {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private Activity activity;
	
	@ManyToOne
	private PlayerList playerList;

	public ActivityPlayerList() {
		super();
	}

	public ActivityPlayerList(Activity activity, PlayerList playerList) {
		super();
		this.activity = activity;
		this.playerList = playerList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public PlayerList getPlayerList() {
		return playerList;
	}

	public void setPlayerList(PlayerList playerList) {
		this.playerList = playerList;
	}
}