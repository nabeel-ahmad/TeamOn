package com.spikenow.enums;

public enum EnumNotificationTypes {

	SIGN_UP("N101"), 
	ACCOUNT_ACTIVATED("N102a"), 
	FB_ACCOUNT_ACTIVATED("N102b"), 
	PASSWORD_RESET_REQUESTED("N103"),
	ACCOUNT_DEACTIVATED("N104"), 
	
	ACTIVITY_CREATED("N105", EnumUserPref.myActivity_created_updated.toString()), 
	ADDED_AS_CO_HOST("N106", EnumUserPref.cohost_invited.toString()),
	ACTIVITY_INVITE_EXSISTING_USER("N107a", EnumUserPref.playerlist_invited.toString()),
	ACTIVITY_INVITE_NEW_USER("N107b"),
	ACTIVITY_CANCELLED("N108", EnumUserPref.myActivity_canceled.toString()),
	JOINED_ACTIVITY_CANCELLED("N109", EnumUserPref.joinedActivity_canceled.toString()),
	ACTIVITY_UPDATED("N110", EnumUserPref.myActivity_created_updated.toString()),
	JOINED_ACTIVITY_UPDATED("N111", EnumUserPref.joinedActivity_canceled.toString()),
	JOIN_REQUEST_SENT("N112", EnumUserPref.joinRequest_sent_withdrawn.toString()),
	JOIN_REQUEST_RECIEVED("N113", EnumUserPref.joinRequest_received_withdrawn.toString()), 
	JOIN_REQUEST_ACCEPTED("N114", EnumUserPref.joinRequest_responded.toString()), 
	JOIN_REQUEST_REJECTED("N115", EnumUserPref.joinRequest_responded.toString()), 
	OWN_JOIN_REQUEST_WITHDRAWN("N116", EnumUserPref.joinRequest_sent_withdrawn.toString()), 
	JOIN_REQUEST_WITHDRAWN("N117", EnumUserPref.joinRequest_received_withdrawn.toString()),
	NEW_COMMENT("N123"),
	
	EMAIL_PARTICIPANTS("N118"), 
	DAILY_DIGEST("N119", EnumUserPref.daily_digest.toString()),
	
	USER_ADDED_TO_PLAYERLIST("N120a", EnumUserPref.added_to_playerlist.toString()),
	NEW_DM("N122", EnumUserPref.new_direct_message.toString()),
	
	NEW_USER_ADDED_TO_PLAYERLIST("N120b"),
	INVITE_TO_TEAMON("N121"); 
	
	
	private String messageKey;
	private String userPref;

	private EnumNotificationTypes(String messageKey) {
		this.messageKey = messageKey;
	}

	private EnumNotificationTypes(String messageKey, String userPref) {
		this.messageKey = messageKey;
		this.userPref = userPref;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getUserPref() {
		return userPref;
	}
	
}
