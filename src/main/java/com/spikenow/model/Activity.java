package com.spikenow.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.spikenow.enums.EnumActivityTypes;
import com.spikenow.util.PropertiesUtil;

/**
 * 
 * @author Nabeel
 *
 */
@XmlRootElement(name = "activity")
@Entity
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Activity {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String description;
	private Date dateTime;
	private String latitude;
	private String longitude;
	private String address;
	private String publishAtEntityType; //group, page or wall
	private String publishAt; // id of the group, page or wall
	private Boolean cancelled = false;
	private Boolean autoAccept = false;


	@Transient
	private List<String> coHostEmails;

	@Transient
	private List<ActivityCoHost> coHosts;

	/**
	 * Write-only: player list ids to be used while saving game
	 */
	@Transient
	private List<Long> playerListIds;

	/**
	 * Read-only: player lists invited to the game
	 */
	@Transient
	private List<ActivityPlayerList> playerLists;
	
	/**
	 * For 1 team, 2 team and practice games
	 */
	@ManyToOne
	private PlayerList playerList1;
	
	/**
	 * Only for 2 team game
	 */
	@ManyToOne
	private PlayerList playerList2;
	
	
	@Transient
	private int commentCount;
	
	private Boolean isPublic = true;
	private Boolean participantsVisible = true;
	private Boolean capacityFilled = false;
	
	private Date createdOn;
	
	/**
	 * Type of game (open, single team, 2 team etc.)
	 */
	private String activityType = EnumActivityTypes.OPEN.getCode();
	
	@ManyToOne
	private ActivityType type;
	
	@ManyToOne
	private User initiatedBy;
	
	@Transient
	private List<Comment> comments;
	
	@Transient
	private Integer responseCount;
	
	@Transient
	private Integer participantCount;
	
	@Transient
	private List<JoinRequest> joinRequsets;
	
	@Transient
	private String joinRequestStatus;
	
	@Transient
	private String tweet;
	
	@Transient
	private Boolean notifyOnUpdate = true;
	
	public Activity() {
		this.dateTime = new Date();
	}
	
	public Activity(String name) {
		this.name = name;
	}

	public Activity(Long id) {
		this.id = id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setInitiatedBy(User initiatedBy) {
		this.initiatedBy = initiatedBy;
	}

	public User getInitiatedBy() {
		return initiatedBy;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLatitude() {
		if(latitude != null)
			return latitude.trim();
		return null;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLongitude() {
		if(longitude != null)
			return longitude.trim();
		return null;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public ActivityType getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public String getLink() throws IOException {
		return PropertiesUtil.getPath("host_address")+"/activity.htm?gameID="+this.getId();
	}
	
	public String getiOSlink() throws IOException {
		return PropertiesUtil.getPath("ios") + "game/" + this.getId();
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Boolean getCancelled() {
		return cancelled;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setResponseCount(Integer responseCount) {
		this.responseCount = responseCount;
	}

	public Integer getResponseCount() {
		return responseCount;
	}

	public void setJoinRequsets(List<JoinRequest> joinRequsets) {
		this.joinRequsets = joinRequsets;
	}

	public List<JoinRequest> getJoinRequsets() {
		return joinRequsets;
	}

	public void setJoinRequestStatus(String joinRequestStatus) {
		this.joinRequestStatus = joinRequestStatus;
	}

	public String getJoinRequestStatus() {
		return joinRequestStatus;
	}

	public void setParticipantCount(Integer participantCount) {
		this.participantCount = participantCount;
	}

	public Integer getParticipantCount() {
		return participantCount;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Boolean getParticipantsVisible() {
		return participantsVisible;
	}

	public void setParticipantsVisible(Boolean participantsVisible) {
		this.participantsVisible = participantsVisible;
	}

	public void setPlayerListIds(List<Long> playerListIds) {
		this.playerListIds = playerListIds;
	}

	public List<Long> getPlayerListIds() {
		return playerListIds;
	}

	public void setCapacityFilled(Boolean capacityFilled) {
		this.capacityFilled = capacityFilled;
	}

	public Boolean getCapacityFilled() {
		return capacityFilled;
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public List<ActivityPlayerList> getPlayerLists() {
		return playerLists;
	}

	public void setPlayerLists(List<ActivityPlayerList> playerLists) {
		this.playerLists = playerLists;
	}

	public void setPublishAt(String publishAt) {
		this.publishAt = publishAt;
	}

	public String getPublishAt() {
		return publishAt;
	}

	public void setCoHostEmails(List<String> coHostIds) {
		this.coHostEmails = coHostIds;
	}

	public List<String> getCoHostEmails() {
		return coHostEmails;
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
		Activity other = (Activity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void setCoHosts(List<ActivityCoHost> coHosts) {
		this.coHosts = coHosts;
	}

	public List<ActivityCoHost> getCoHosts() {
		return coHosts;
	}

	public void setPublishAtEntityType(String publishAtEntityType) {
		this.publishAtEntityType = publishAtEntityType;
	}

	public String getPublishAtEntityType() {
		return publishAtEntityType;
	}

	public Boolean getAutoAccept() {
		return autoAccept == null ? false : autoAccept;
	}

	public void setAutoAccept(Boolean autoAccept) {
		this.autoAccept = autoAccept;
	}


	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public Boolean getNotifyOnUpdate() {
		return notifyOnUpdate;
	}

	public void setNotifyOnUpdate(Boolean notifyOnUpdate) {
		this.notifyOnUpdate = notifyOnUpdate;
	}

	public String getActivityType() {
		return activityType != null ? activityType : EnumActivityTypes.OPEN.getCode();
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public PlayerList getPlayerList1() {
		return playerList1;
	}

	public void setPlayerList1(PlayerList playerList1) {
		this.playerList1 = playerList1;
	}

	public PlayerList getPlayerList2() {
		return playerList2;
	}

	public void setPlayerList2(PlayerList playerList2) {
		this.playerList2 = playerList2;
	}

}
