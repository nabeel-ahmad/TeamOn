package com.spikenow.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.spikenow.dao.SpikeDAO;
import com.spikenow.dao.TeamOnException;
import com.spikenow.enums.EnumActivityTypes;
import com.spikenow.enums.EnumNotificationTypes;
import com.spikenow.enums.EnumServerError;
import com.spikenow.enums.EnumStatusTypes;
import com.spikenow.enums.EnumUserPref;
import com.spikenow.model.Activity;
import com.spikenow.model.ActivityCoHost;
import com.spikenow.model.ActivityType;
import com.spikenow.model.Comment;
import com.spikenow.model.DataList;
import com.spikenow.model.Device;
import com.spikenow.model.DirectMessage;
import com.spikenow.model.DirectMessageThread;
import com.spikenow.model.Email;
import com.spikenow.model.JoinRequest;
import com.spikenow.model.Notification;
import com.spikenow.model.NotificationRecipient;
import com.spikenow.model.Password;
import com.spikenow.model.Player;
import com.spikenow.model.PlayerList;
import com.spikenow.model.Subscription;
import com.spikenow.model.User;
import com.spikenow.model.UserActivity;
import com.spikenow.model.UserPreferences;
import com.spikenow.util.MailUtil;
import com.spikenow.util.PropertiesUtil;
import com.spikenow.util.Util;

/**
 * 
 * @author Nabeel
 * 
 */
@Transactional
public class SpikeService {

	@Autowired
	private SpikeDAO spikeDAO;
	
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	private Logger logger = Logger.getLogger("com.spikenow.service");
	
	/**
	 * 
	 * @param lat
	 * @param lng
	 * @param radius 
	 * @return
	 */
	public DataList<Activity> fetchActivities(Double lat, Double lng, Double radius) {
		try {
			List<Activity> activities = spikeDAO.listActivities(lat, lng, radius);
			for (Activity activity : activities) {
				activity.setCoHosts(spikeDAO.getActivityCoHosts(activity.getId()));
			}
			return new DataList<Activity>(activities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataList<Activity> fetchActivities(String userAuth, String authProvider, Double lat, Double lng, Double radius) {
		try {
			String userId = null;
			userId = getUser(userAuth, authProvider).getId();
			if (userId == null)
				throw new Exception("Inavlid User");
			List<Activity> activities = spikeDAO.listActivities(lat, lng, radius);
			for (Activity activity : activities) {
				JoinRequest jr = spikeDAO.getJoinRequest(activity.getId(), userId);
				activity.setCoHosts(spikeDAO.getActivityCoHosts(activity.getId()));
				if (jr != null) {
					activity.setJoinRequestStatus(jr.getStatus());
				}
			}
			return new DataList<Activity>(activities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Transactional(readOnly = true)
	public DataList<Activity> fetchMyActivities(String userAuth, String authProvider, Integer offset, String categoty) {
		try {
			String userId = null;
			userId = getExistingUser(userAuth, authProvider).getId();
			if (userId == null)
				throw new Exception("Inavlid User");
			List<Activity> activities = spikeDAO.fetchMyActivities(userId, offset, categoty);
			for (Activity activity : activities) {
				activity.setPlayerLists(spikeDAO.fetchActivityPlayerLists(activity.getId()));
				activity.setCoHosts(spikeDAO.getActivityCoHosts(activity.getId()));
			}
			return new DataList<Activity>(activities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<JoinRequest> fetchMyJoinRequests(String userAuth, String authProvider, Integer page, String category) {
		try {
			String userId = null;
			userId = getUser(userAuth, authProvider).getId();
			return spikeDAO.fetchMyJoinRequests(userId, page, category);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Transactional(readOnly = true)
	public Activity fetchFirstUpcomingActivity(String userAuth, String authProvider) throws Exception {
		User user = getExistingUser(userAuth, authProvider);
		Activity activity = spikeDAO.fetchFirstUpcomingActivity(user.getId(), false);
		activity.setCoHosts(spikeDAO.getActivityCoHosts(activity.getId()));
		if(activity.getInitiatedBy().getId().equals(user.getId()) || activity.getCoHosts().contains(new ActivityCoHost(activity, user))) {
			activity.setPlayerLists(spikeDAO.fetchActivityPlayerLists(activity.getId()));
			List<JoinRequest> joinRequests = spikeDAO.fetchJoinRequests(activity.getId(), user.getId());
			for (JoinRequest joinRequest : joinRequests) {
				joinRequest.setActivity(null);
			}
			activity.setJoinRequsets(joinRequests);
		} else {
			JoinRequest jr = spikeDAO.getJoinRequest(activity.getId(), user.getId());
			activity.setJoinRequestStatus(jr.getStatus());
		}
		activity.setCommentCount(spikeDAO.fetchCommentCount(activity.getId()));
		activity.setComments(spikeDAO.fetchComments(activity.getId(), 1));
		return activity;
	}

	/**
	 * 
	 * @return
	 */
	public DataList<ActivityType> fetchActivityTypes() {
		try {
			List<ActivityType> list = spikeDAO.listActivityTypes();
			ActivityType custom = null;
			for (ActivityType activityType : list) {
				if(activityType.getName().equalsIgnoreCase("other")) {
					custom = activityType;
				}
			}
			if(custom != null) {
				// Put custom at the top
				list.remove(custom);
				list.add(list.size(), custom);
			}
			return new DataList<ActivityType>(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataList<Notification> fetchNotifications(Boolean unread, String userAuth, String authProvider) {
		try {
			List<Notification> list = spikeDAO.listNotifications(unread, getUser(userAuth, authProvider).getId());
			return new DataList<Notification>(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataList<Notification> fetchNotifications(Long activityId, Boolean unread, String userAuth, String authProvider) {
		try {
			List<Notification> list = spikeDAO.listNotifications(activityId, unread, getUser(userAuth, authProvider).getId());
			return new DataList<Notification>(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataList<JoinRequest> fetchJoinRequests(Long activityId, String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			if (user != null) {
				List<JoinRequest> list = spikeDAO.fetchJoinRequests(activityId, user.getId());
				return new DataList<JoinRequest>(list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param activity
	 * @param authProvider
	 * @param userAuth
	 * @return
	 * @throws Exception
	 */
	public Activity saveActivity(Activity activity, String userAuth, String authProvider) throws Exception {

		User user = getUser(userAuth, authProvider);
		if (user == null)
			return null;

		List<ActivityCoHost> existingCoHosts = null;
		if (activity.getId() != null) {
			existingCoHosts = spikeDAO.getActivityCoHosts(activity.getId());
			Activity old = spikeDAO.fetchActivity(activity.getId());
			ActivityCoHost ach = new ActivityCoHost(old, user);
			if (old.getInitiatedBy() != user &&	!existingCoHosts.contains(ach)) {
				return null;
			}
			activity.setInitiatedBy(old.getInitiatedBy());
		} else {
			activity.setInitiatedBy(user);
			if (activity.getName() == null) {
				activity.setName(spikeDAO.getActivityType(activity.getType().getId()).getName());
			}
			activity.setCreatedOn(new Date());
			if (activity.getAddress() == null || activity.getAddress().equals("")) {
				activity.setAddress(Util.getAddress(activity.getLatitude(), activity.getLongitude()));
			}
		}

		Activity result = spikeDAO.saveActivity(activity);

		if (result != null) {
			if (activity.getId() == null) {
				sendNotification(user, result.getInitiatedBy(), result, EnumNotificationTypes.ACTIVITY_CREATED);
			} else {
				String[] categories = {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()};
				List<User> participants = spikeDAO.fetchActivityParticipants(result.getId(), Arrays.asList(categories));

				if(user.getId() != result.getInitiatedBy().getId()) {
					// Notify Organizer
					sendNotification(user, result.getInitiatedBy(), result, EnumNotificationTypes.JOINED_ACTIVITY_UPDATED);
				}
				// Notify logged in user
				sendNotification(user, user, result, EnumNotificationTypes.ACTIVITY_UPDATED);
				
				List<User> recipients = new ArrayList<User>();
				for (User p : participants) {
					recipients.add(p);
				}
				if(activity.getNotifyOnUpdate() == null || activity.getNotifyOnUpdate() == true)
					sendNotification(user, recipients, result, EnumNotificationTypes.JOINED_ACTIVITY_UPDATED);
			}

			// Save Player lists for new activity and send invitations to player lists
			if (activity.getPlayerListIds() != null && activity.getPlayerListIds().size() > 0 && activity.getId() == null) {
				List<PlayerList> lists = spikeDAO.getPlayerListsByIds(activity.getPlayerListIds());
				spikeDAO.saveActivityPlayerLists(result, lists);

				List<String> invitedEmails = new ArrayList<String>();
				List<User> invitedUsers = new ArrayList<User>();
				for (PlayerList playerList : lists) {
					for (Player player : playerList.getPlayers()) {
						if(player.getUser() == null)
							invitedEmails.add(player.getEmail());
						else
							invitedUsers.add(player.getUser());
					}
				}
				
				// Notify Registered guests
				if(!Util.isEmpty(invitedUsers)) {
					sendNotification(user, invitedUsers, result, EnumNotificationTypes.ACTIVITY_INVITE_EXSISTING_USER);
				}
				
				if(!Util.isEmpty(invitedEmails)) {
					// Notify Unregistered guests by email
					final List<Email> emails = new ArrayList<Email>();
					Email emailTemplate = buildEmail(user, null, result, EnumNotificationTypes.ACTIVITY_INVITE_NEW_USER, null);
					for (String address : invitedEmails) {
						Email email = new Email(address, emailTemplate.getSubject(), emailTemplate.getBody());
						emails.add(email);
					}
					threadPool.execute(new Runnable() {
						public void run() {
							MailUtil.sendEmails(emails);
						}});
				}
				
			}

			// Only for team based games, add player list owners as co-hosts
			if(!activity.getActivityType().equals(EnumActivityTypes.OPEN.getCode())) {
				if(activity.getCoHostEmails() == null)
					activity.setCoHostEmails(new ArrayList<String>());
				
				if(activity.getPlayerList1() != null) {
					String cohostID = activity.getPlayerList1().getUser().getId();
					if(!activity.getCoHostEmails().contains(cohostID))
						activity.getCoHostEmails().add(cohostID);
				}
				if(activity.getPlayerList2() != null) {
					String cohostID = activity.getPlayerList2().getUser().getId();
					if(!activity.getCoHostEmails().contains(cohostID))
						activity.getCoHostEmails().add(cohostID);
				}
			}
			
			if (activity.getCoHostEmails() != null) {				
				spikeDAO.saveActivityCoHosts(result.getId(), activity.getCoHostEmails(), existingCoHosts);
			}

			result.setCoHosts(spikeDAO.getActivityCoHosts(result.getId()));

			if (result.getCoHosts() != null) {
				List<User> recipients = new ArrayList<User>();
				for (ActivityCoHost ch : result.getCoHosts()) {
					// Send email if co-host is added for the first time
					if (existingCoHosts == null || !existingCoHosts.contains(ch))
						recipients.add(ch.getUser());
				}
				sendNotification(user, recipients, result, EnumNotificationTypes.ADDED_AS_CO_HOST);
			}

			String tweet = Util.getTemplate("twitter-game-created.tl");
			tweet = tweet.replace("[user-name]", result.getInitiatedBy().getName());
			tweet = tweet.replace("[sport]", result.getType().getName());
			Float timezone = result.getInitiatedBy().getTimezone();
			if(timezone == null) {
				timezone = Util.getTimezone(activity.getLatitude(), activity.getLongitude());
			}
			tweet = tweet.replace("[game-date]", Util.format(result.getDateTime(), timezone));
			tweet = tweet.replace("[game-location]", result.getAddress());
			tweet = tweet.replace("[link]", result.getLink());
			result.setTweet(tweet);
			
			if(result.getIsPublic())
				notifySubscribers(result);

			return result;
		}
		return null;
	}
	
	private void notifySubscribers(final Activity result) throws IOException {
		List<Subscription> subs = spikeDAO.getSubscribers(result);
		if(Util.isEmpty(subs)) return;
		final List<Email> emails = new ArrayList<Email>();
		final List<Device> iosDevices = new ArrayList<Device>();
		final List<String> androidDevices = new ArrayList<String>();
		final String subjTemplate = PropertiesUtil.getMessage("N124");
		String emailTxt = Util.getTemplate("N124.html");
		emailTxt = Util.substituteValues(emailTxt, result.getInitiatedBy(), result, null);
		final String subject = Util.substituteSubjectValues(subjTemplate, null, result);
		for (Subscription sub : subs) {
			Double x1 = new Double(result.getLatitude());
			Double x2 = new Double(sub.getLatitude());
			Double y1 = new Double(result.getLongitude());
			Double y2 = new Double(sub.getLongitude());
			Double dist = Util.distance(x1, y1, x2, y2);
			if(dist <= sub.getMaxDistance()) {
				if(sub.isEmail())
					emails.add(new Email(sub.getUser().getEmail(), subject, emailTxt));
				if(sub.isPush()) {
					List<Device> devices = spikeDAO.fetchUserDevices(sub.getUser().getId());
					for (Device device : devices) {
						if(device.getDevice_type().equals("ios")) {
							device.setUnreadCount(spikeDAO.fetchNotificationCount(device.getUser().getId()));
							iosDevices.add(device);
						}
						if(device.getDevice_type().equals("android")) {
							androidDevices.add(device.getDevice_id());
						}
					}
				}
					
			}
		}
		
		threadPool.execute(new Runnable() {
			public void run() {
				try {
					if(emails.size() > 0) {
						MailUtil.sendEmails(emails);
					}
					if(iosDevices.size() > 0) {
						String link = result == null ? null : result.getiOSlink();
						sendAPN(subject, link, iosDevices, null);
					}
					if(androidDevices.size() > 0) {
						String link = result == null ? null : result.getLink();
						sendGCM(subject, link, androidDevices, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Activity getActivity(Long id) {
		try {
			Activity result = spikeDAO.fetchPublicActivity(id);
			if(result != null) {
				result.setCoHosts(spikeDAO.getActivityCoHosts(result.getId()));
				result.setCommentCount(spikeDAO.fetchCommentCount(result.getId()));
				result.setComments(spikeDAO.fetchComments(result.getId(), 1));
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Transactional(readOnly = true)
	public Activity getMyActivity(Long id, String userAuth, String authProvider) throws Exception {
			User user = getExistingUser(userAuth, authProvider);
			Activity activity = spikeDAO.fetchMyActivity(id, user);
			activity.setCoHosts(spikeDAO.getActivityCoHosts(id));
			List<JoinRequest> joinRequests = spikeDAO.fetchJoinRequests(activity.getId(), user.getId());
			activity.setJoinRequsets(joinRequests);
			for (JoinRequest joinRequest : joinRequests) {
				joinRequest.setActivity(null);
			}
			activity.setCommentCount(spikeDAO.fetchCommentCount(activity.getId()));
			activity.setComments(spikeDAO.fetchComments(activity.getId(), 1));
			
			if(spikeDAO.isHost(activity, user))
				activity.setPlayerLists(spikeDAO.fetchActivityPlayerLists(activity.getId()));
			return activity;
	}

	public String joinActivity(Long activityId, String userAuth, String authProvider) {
		try {
			if (activityId == null) {
				return "Activity id is missing";
			}
			User user = getUser(userAuth, authProvider);
			if (user != null) {
				JoinRequest jr = spikeDAO.getJoinRequest(activityId, user.getId());
				if(jr != null && !jr.getStatus().equals(EnumStatusTypes.WITHDRAWN.toString())) {
					// join request already exists
					return PropertiesUtil.getMessage("dialog-join-req-pending");
				}
				
				// No join request exists or is withdrawn
				Activity activity = spikeDAO.fetchActivity(activityId);

				if (activity.getCancelled())
					return "This game has been cancelled";
				if (activity.getCapacityFilled())
					return PropertiesUtil.getMessage("dialog-game-full");

				if (jr == null) {
					jr = new JoinRequest();
					jr.setActivity(activity);
					jr.setUser(user);
				}
				if(activity.getAutoAccept())
					jr.setStatus(EnumStatusTypes.ACCEPTED.toString());
				else
					jr.setStatus(EnumStatusTypes.NO_RESPONSE.toString());
				jr.setCreatedOn(new Date());
				JoinRequest result = spikeDAO.saveJoinRequest(jr);
				
				if(activity.getAutoAccept()) {
					sendNotification(user, user, result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_ACCEPTED);
					return "Your join request has been accepted";
				}

				if (result != null) {
					List<ActivityCoHost> cohosts = spikeDAO.getActivityCoHosts(activity.getId());
					List<User> cohostUsers = new ArrayList<User>();
					for (ActivityCoHost activityCoHost : cohosts) {
						cohostUsers.add(activityCoHost.getUser());
					}
					if(result.getActivity().getActivityType().equals(EnumActivityTypes.TWO_TEAM.getCode())) {
						// For 2 team games join request notification should only go to corresponding player list owner  
						boolean inPL1 = spikeDAO.playerListHasUser(result.getActivity().getPlayerList1().getId(), user.getId());
						if(inPL1)
							sendNotification(user, result.getActivity().getPlayerList1().getUser(), result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_RECIEVED, result, true);
						else
							sendNotification(user, result.getActivity().getPlayerList2().getUser(), result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_RECIEVED, result, true);
					} else {
						sendNotification(user, result.getActivity().getInitiatedBy(), result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_RECIEVED, result, true);
						sendNotification(user, cohostUsers, result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_RECIEVED);
					}
					sendNotification(user, user, result.getActivity(), EnumNotificationTypes.JOIN_REQUEST_SENT);
				}
				return "Join request sent to " + result.getActivity().getInitiatedBy().getFirst_name()
				+ ". You will be notified once the request is approved.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String cancelActivity(Long id, String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			if (user != null) {
				Activity activity = spikeDAO.fetchActivity(id);
				if (activity != null && spikeDAO.isHost(activity, user)) {
					activity.setCancelled(true);
					Set<User> recipients = new HashSet<User>();
					List<JoinRequest> joinRequests = spikeDAO.getJoinRequests(activity.getId());
					if (joinRequests != null) {
						for (JoinRequest jr : joinRequests) {
							if (jr.getStatus().equals(EnumStatusTypes.ACCEPTED.toString()) || jr.getStatus().equals(EnumStatusTypes.NO_RESPONSE.toString())) {
								recipients.add(jr.getUser());
							}
						}
						sendNotification(user, recipients, activity, EnumNotificationTypes.JOINED_ACTIVITY_CANCELLED);
					}
					
					sendNotification(user, user, activity, EnumNotificationTypes.ACTIVITY_CANCELLED);
					if(user.getId() != activity.getInitiatedBy().getId()) {
						sendNotification(user, activity.getInitiatedBy(), activity, EnumNotificationTypes.JOINED_ACTIVITY_CANCELLED);
					}
					
					return activity.getName() + " has been cancelled";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public JoinRequest respondToRequest(Long id, Long notificationId, String status, String userAuth, String authProvider) {
		try {
			if (Util.isStatusValid(status)) {
				JoinRequest joinRequest = spikeDAO.getJoinRequest(id);
				if (joinRequest != null && !joinRequest.getActivity().getCancelled()) {
					User user = getUser(userAuth, authProvider);
					if (user == null)
						return null;

					if (spikeDAO.isHost(joinRequest.getActivity(), user)) {

						joinRequest.setStatus(status);
						joinRequest.setCreatedOn(new Date()); // TODO: add updatedOn parameter in JoinRequest class
						if (notificationId != null) {
							this.fetchNotification(notificationId, userAuth, authProvider);
						}

						EnumNotificationTypes type = null;
						if (status.equals(EnumStatusTypes.ACCEPTED.toString())) {
							type = EnumNotificationTypes.JOIN_REQUEST_ACCEPTED;
						} else if (status.equals(EnumStatusTypes.REJECTED.toString())) {
							type = EnumNotificationTypes.JOIN_REQUEST_REJECTED;
						} 
						
						if(type != null)
							sendNotification(user, joinRequest.getUser(), joinRequest.getActivity(), type);

						return joinRequest;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String withdrawRequestById(Long id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		JoinRequest joinRequest = spikeDAO.getJoinRequest(id);
		return withdrawRequest(joinRequest, user);
	}

	public String withdrawRequestByActivityId(Long activityId, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		JoinRequest joinRequest = spikeDAO.getJoinRequest(activityId, user.getId());
		return withdrawRequest(joinRequest, user);
	}
	
	@SuppressWarnings("unchecked")
	public String markPlayed(Long activityId, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		JoinRequest joinRequest = spikeDAO.getJoinRequest(activityId, user.getId());
		if (joinRequest != null) {
			if (user != null && user.getId() == joinRequest.getUser().getId()) {
				joinRequest.setStatus(EnumStatusTypes.PLAYED.toString());
				joinRequest.setCreatedOn(new Date()); // TODO: add updatedOn parameter in JoinRequest class
				List<UserPreferences> prefs = (List<UserPreferences>) fetchUserPrefs(user).get("social");
				UserPreferences p = new UserPreferences(EnumUserPref.fb_publish_on_timeline_play.toString(), user);
				Boolean publishAction = new Boolean(prefs.get(prefs.indexOf(p)).getValue());
				if(publishAction)
					Util.postFacebookPlayAction(joinRequest.getActivity(), userAuth);
				return EnumStatusTypes.PLAYED.toString();
			}
		}
		return null;
	}
	
	private String withdrawRequest(JoinRequest joinRequest, User user) throws Exception {
		if (joinRequest != null) {
			if (user != null && user.getId() == joinRequest.getUser().getId()) {
				joinRequest.setStatus(EnumStatusTypes.WITHDRAWN.toString());
				joinRequest.setCreatedOn(new Date()); // TODO: add updatedOn parameter in JoinRequest class
				if (!joinRequest.getStatus().equals(EnumStatusTypes.NO_RESPONSE.toString())) {
					sendNotification(user, joinRequest.getActivity().getInitiatedBy(), joinRequest.getActivity(), EnumNotificationTypes.JOIN_REQUEST_WITHDRAWN);
					sendNotification(user, user, joinRequest.getActivity(), EnumNotificationTypes.OWN_JOIN_REQUEST_WITHDRAWN);

					List<ActivityCoHost> cohosts = spikeDAO.getActivityCoHosts(joinRequest.getActivity().getId());
					List<User> recipients = new ArrayList<User>();
					for (ActivityCoHost activityCoHost : cohosts) {
						recipients.add(activityCoHost.getUser());
					}
					sendNotification(user, recipients, joinRequest.getActivity(), EnumNotificationTypes.JOIN_REQUEST_WITHDRAWN);
				}
				return PropertiesUtil.getMessage("dialog-join-req-withdrawn");
			}
		}
		return null;
	}

	public User getMyProfile(String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		Integer organizedGameCount = spikeDAO.getOrganizedGameCountByUser(user.getId());
		user.setOrganizedGameCount(organizedGameCount);

		Integer joinedGameCount = spikeDAO.getJoinedGameCountByUser(user.getId());
		user.setJoinedGameCount(joinedGameCount);
		return user;
	}
	
	public User getUser(String userAuth, String authProvider) throws Exception {
		
		User user = spikeDAO.getUser(userAuth);
		if (user != null && user.getId() != null) {
			user.setFirstLogin(false);
			return user;
		}
		
		if(user == null) {
			if (authProvider.equals("google.com")) {
				user = Util.authenticateUser(authProvider, userAuth, "id,name,given_name,gender,link,picture,email");
			}
			if (authProvider.equals("facebook.com")) {
				user = Util.authenticateUser(authProvider, userAuth, "id,name,first_name,gender,link,picture,email,timezone");
			}
		}
		
		if (user != null && user.getId() != null) {
			user.setAccess_token(userAuth);
			user.setAuthProvider(authProvider);
			User tmp = spikeDAO.getUserById(user.getId());
			
			// flag to show first login for facebook users
			Boolean isFirstLogin = false;
			
			if (tmp == null) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("-userFullname-", user.getName());
				if (authProvider.equals("facebook.com")) {
					MailUtil.sendEmail(user.getEmail(), EnumNotificationTypes.FB_ACCOUNT_ACTIVATED, map);
				}
				user = spikeDAO.mergeUser(user);
				PlayerList defaultPL = new PlayerList();
				defaultPL.setName("default");
				defaultPL.setUser(user);
				spikeDAO.savePlayerList(defaultPL, user.getId());
				
				return user;
			} else {
				isFirstLogin = tmp.getFirstLogin();
				if(user.getAddress() == null && tmp.getAddress() != null)
					user.setAddress(tmp.getAddress());
				if(user.getLatitude() == null && tmp.getLatitude() != null)
					user.setLatitude(tmp.getLatitude());
				if(user.getLongitude() == null && tmp.getLongitude() != null)
					user.setLongitude(tmp.getLongitude());
			}
			user.setFirstLogin(false);
			User result = spikeDAO.mergeUser(user);
			
			result.setFirstLogin(isFirstLogin);
			
			return result;
		}

		if(user == null)
			throw new TeamOnException(EnumServerError.INVALID_USER);
		else
			return user;
	}

	public User getExistingUser(String userAuth, String authProvider) throws Exception {
		User user = spikeDAO.getUser(userAuth);
		if (user != null && user.getId() != null) {
			return user;
		}
		
		if (authProvider.equals("google.com")) {
			user = Util.authenticateUser(authProvider, userAuth, "id,name,given_name,gender,link,picture,email");
		}
		if (authProvider.equals("facebook.com")) {
			user = Util.authenticateUser(authProvider, userAuth, "id,name,first_name,gender,link,picture,email");
		}
		if (user != null) {
			user.setAuthProvider(authProvider);
			return spikeDAO.getExistingUser(user.getId());
		}

		throw new TeamOnException(EnumServerError.INVALID_USER);
	}

	public Notification fetchNotification(Long id, String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			if (user != null) {
				return spikeDAO.fetchNotification(id, user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Integer fetchNotificationCount(String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			return spikeDAO.fetchNotificationCount(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<User> fetchActivityParticipants(Long id, String userAuth, String authProvider) {
		try {
//			User user = getUser(userAuth, authProvider);
			return spikeDAO.fetchActivityVisibleParticipants(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String, Object> fetchUserPrefs(String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return fetchUserPrefs(user);
	}

	public Map<String, Object> fetchUserPrefs(User user) throws Exception {
		if (user != null) {
			List<UserPreferences> list = spikeDAO.fetchUserPrefs(user.getId());
			List<UserPreferences> notifPrefs = new ArrayList<UserPreferences>();
			List<UserPreferences> socialPrefs = new ArrayList<UserPreferences>();
			Map<String, Object> map = new HashMap<String, Object>();
			
			for (EnumUserPref eup : EnumUserPref.values()) {
				UserPreferences p = new UserPreferences(user, eup, !eup.getPushDisabled(), true);

				int index = list.indexOf(p);
				if (index < 0)
					list.add(p);
				else {
					list.get(index).setLabel(p.getLabel());
					list.get(index).setPushDisabled(p.getPushDisabled());
					p = list.get(index);
				}
				if(eup.toString().startsWith("fb")) {
					socialPrefs.add(p);
				} else {
					notifPrefs.add(p);
				}
			}
			
			map.put("notifications", notifPrefs);
			map.put("social", socialPrefs);

			return map;
		}
		return null;
	}

	public String saveUserPrefs(List<UserPreferences> list, String userAuth, String authProvider) throws Exception {
		String userId = null;
		userId = getUser(userAuth, authProvider).getId();
		if (userId != null) {
			return spikeDAO.saveUserPrefs(userId, list);
		}
		return null;
	}

	public PlayerList savePlayerList(PlayerList playerList, String userAuth, String authProvider) throws Exception {
			final User user = getUser(userAuth, authProvider);
			if (user != null) {
				PlayerList result = spikeDAO.savePlayerList(playerList, user.getId());
				for (final Player player : playerList.getPlayers()) {
					if(player.getId() == null) {
						if(player.getUser() != null) {
							sendNotification(user, player.getUser(), null, EnumNotificationTypes.USER_ADDED_TO_PLAYERLIST);
						} else {
							threadPool.execute(new Runnable() {
								public void run() {
									try {
										sendEmail(user, player.getEmail(), null, EnumNotificationTypes.NEW_USER_ADDED_TO_PLAYERLIST, null);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				}
				return result;
			}
		return null;
	}

	public List<PlayerList> fetchPlayerLists(String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			if (user != null) {
				return spikeDAO.fetchPlayerLists(user.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public User signup(User user, String password) throws Exception {
		try {
			if (user.getEmail() == null)
				throw new Exception("Invalid email field");

			user.setId(user.getEmail());
			user.setPassword(Util.digest(password));
			user.setAuthProvider("teamonapp.com");
			user.setActivationCode(UUID.randomUUID().toString());
			spikeDAO.signup(user);
			String link = PropertiesUtil.getPath("host_address") + "/activate/" + user.getId() + "/" + user.getActivationCode();

			Map<String, String> map = new HashMap<String, String>();
			map.put("-activationURL-", link);
			map.put("-emailaddress-", user.getEmail());

			MailUtil.sendEmail(user.getEmail(), EnumNotificationTypes.SIGN_UP, map);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public User updateProfile(User userProfile, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		if (user == null)
			return null;
		return spikeDAO.updateProfile(user, userProfile);
	}

	public User activateUser(String email, String code) {
		User user = spikeDAO.activateUser(email, code);
		if (user != null) {
			try {
				Map<String, String> map = new HashMap<String, String>();
				map.put("-emailaddress-", user.getEmail());
				map.put("-userFullname-", user.getName());
				MailUtil.sendEmail(user.getEmail(), EnumNotificationTypes.ACCOUNT_ACTIVATED, map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return user;
	}

	public Boolean deleteAccount(String userAuth, String authProvider) {
		try {
			User user = getUser(userAuth, authProvider);
			if (user == null)
				return false;
			user.setActivationCode(UUID.randomUUID().toString());
			user.setAccess_token(null);

			Map<String, String> map = new HashMap<String, String>();
			map.put("-emailaddress-", user.getEmail());
			MailUtil.sendEmail(user.getEmail(), EnumNotificationTypes.ACCOUNT_DEACTIVATED, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public Map<String, Object> signin(String email, String pwd) throws Exception {
		return spikeDAO.signin(email, Util.digest(pwd));
	}

	public void resetPassword(String email) throws Exception {
		try {
			User user = spikeDAO.getUserById(email);
			if (user != null && user.getAuthProvider().equals("teamonapp.com")) {
				String token = UUID.randomUUID().toString();
				user.setAccess_token(token);
				String url = PropertiesUtil.getPath("host_address") + "/update-password/" + token;

				Map<String, String> map = new HashMap<String, String>();
				map.put("-resetURL-", url);
				map.put("-emailaddress-", user.getEmail());
				MailUtil.sendEmail(user.getEmail(), EnumNotificationTypes.PASSWORD_RESET_REQUESTED, map);

				return;
			}
			throw new Exception("User does not exist");
		} catch (Exception e) {
			throw e;
		}
	}

	public Map<String, String> fetchStaticMessages(String platform) {
		Map<String, String> map = new HashMap<String, String>();

		/*
		 * map.put("delete-account", Util.getTemplate("delete-account.tl"));
		 * map.put("about", Util.getTemplate("about.tl")); map.put("contact",
		 * Util.getTemplate("contact.tl")); map.put("twitter-game-created",
		 * Util.getTemplate("twitter-game-created.tl"));
		 * 
		 * map.put("share-teamon-email",
		 * Util.getTemplate("share-teamon-email.html"));
		 * map.put("share-teamon-facebook",
		 * Util.getTemplate("share-teamon-facebook.tl"));
		 * map.put("share-teamon-sms", Util.getTemplate("share-teamon-sms.tl"));
		 * map.put("share-teamon-twitter",
		 * Util.getTemplate("share-teamon-twitter.tl"));
		 */

		try {
			Properties dialogs = null;
			
			if(platform.equals("ios")) {
				dialogs = PropertiesUtil.getIosMessages();
				for (Object key : dialogs.keySet()) {
					map.put(key.toString(), dialogs.get(key).toString());
				}
			} else if(platform.equals("android")) {
				dialogs = PropertiesUtil.getAndroidMessages();
				for (Object key : dialogs.keySet()) {
					map.put(key.toString(), dialogs.get(key).toString());
				}
			} else if(platform.equals("web")) {
				dialogs = PropertiesUtil.getWebMessages();
				for (Object key : dialogs.keySet()) {
					map.put(key.toString(), dialogs.get(key).toString());
				}
			}
			
			return map;
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerSharingAction(String action, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		if (user == null)
			return;
		spikeDAO.registerSharingAction(action, user, new Date());
	}

	public boolean deletePlayerLists(Long[] ids, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		if (user == null)
			return false;
		return spikeDAO.deletePlayerLists(ids, user.getId());
	}

	public User saveUserPic(MultipartFile file, String userAuth, String authProvider) throws Exception {
		if (!authProvider.equals("teamonapp.com"))
			return null;
		User user = getUser(userAuth, authProvider);
		if (user == null)
			return null;
		if (!file.getContentType().startsWith("image"))
			return null;

		String filename = UUID.randomUUID().toString();
		if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
			filename += file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
		}
		FileUtils.writeByteArrayToFile(new File(PropertiesUtil.getPath("images_store") + "/" + filename), file.getBytes());
		user.setPicture(PropertiesUtil.getPath("images") + filename);
		return user;
	}

	public String savePLPic(Long plId, MultipartFile file, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		if (!file.getContentType().startsWith("image"))
			throw new TeamOnException(EnumServerError.INVALID_REQUEST);
		
		String filename = UUID.randomUUID().toString();
		if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
			filename += file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
		}
		FileUtils.writeByteArrayToFile(new File(PropertiesUtil.getPath("images_store") + "/" + filename), file.getBytes());
		PlayerList pl = spikeDAO.getPlayerList(plId, user.getId());
		pl.setPicture(PropertiesUtil.getPath("images") + filename);
		return pl.getPicture();
	}

	public boolean updatePassword(Password password) {
		try {
			User user = getUser(password.getAccess_token(), "teamonapp.com");
			if (user == null || password.getPassword().length() < 6 || !password.getPassword().equals(password.getPassword2()))
				return false;
			user.setPassword(Util.digest(password.getPassword()));
			user.setAccess_token(UUID.randomUUID().toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void emailParticipants(Long activityId, final String message, String categories, String userAuth, String authProvider) throws Exception {
		final User user = getUser(userAuth, authProvider);
		if (user == null)
			return;
		final Activity act = spikeDAO.fetchActivity(activityId);
		if (act.getInitiatedBy().getId() != user.getId())
			throw new Exception();
		StringTokenizer st = new StringTokenizer(categories, ",");
		List<String> tokens = new ArrayList<String>();
		while(st.hasMoreTokens())
			tokens.add(st.nextToken());
		final List<User> participants = spikeDAO.fetchActivityParticipants(activityId, tokens);
		threadPool.execute(new Runnable() {
			public void run() {
				for (User p : participants) {
					try {
						sendEmail(user, p.getEmail(), act, EnumNotificationTypes.EMAIL_PARTICIPANTS, message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}});
	}

	public void registerDevice(Device device, String userAuth, String authProvider) throws Exception {
		if (device.getDevice_id() == null || device.getDevice_type() == null)
			throw new Exception("Device information is incomplete");

		User user = getUser(userAuth, authProvider);
		if (user == null)
			return;

		device.setUser(user);
		spikeDAO.registerDevice(device);
	}

	public void unregisterDevice(String id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		if (user == null)
			return;
		Device device = new Device();
		device.setDevice_id(id);
		device.setUser(user);
		spikeDAO.unregisterDevice(device);
	}
	
	public void sendDailyDigestEmails() throws Exception {
		List<User> users = spikeDAO.fetchActiveUsers();
		if(users == null) return;
		
		String emailTemplate = Util.getTemplate(EnumNotificationTypes.DAILY_DIGEST.getMessageKey() + ".html");
		
		for (User user : users) {
			List<Activity> upcomingActivities = spikeDAO.fetchDailyUpcomingActivities(user.getId());
			List<Activity> recentActivities = spikeDAO.fetchDailyRecentActivities(user.getId());
			
			if(Util.isEmpty(recentActivities) && Util.isEmpty(upcomingActivities))
				continue;
			else {
				String message = emailTemplate;
				String upcomingHtml = MailUtil.buildUpcomingDigestHtml(upcomingActivities);
				String recentHtml = MailUtil.buildRecentDigestHtml(recentActivities);
				message = message.replace("-upcomingGamesHTML-", upcomingHtml);
				message = message.replace("-recentGamesHTML-", recentHtml);
				message = message.replace("-senderFullname-", user.getName());
				
				sendEmailByTimezone(user, message);
			}
		}
	}
	
	public Boolean[] checkRegisteredUserEmails(String[] emails, String userAuth, String authProvider) throws Exception {
		return spikeDAO.checkRegisteredUserEmails(emails);
	}

	private void sendEmailByTimezone(User user, String message) throws Exception {
		Boolean pushEmail = true;
		List<UserPreferences> prefs = spikeDAO.fetchUserPrefs(user.getId());
		int index = prefs.indexOf(new UserPreferences(EnumNotificationTypes.DAILY_DIGEST.getUserPref(), user));
		if(index >= 0) {
			pushEmail = prefs.get(index).getEmailNotification();
		}
		if(!pushEmail) return;
		
		Calendar userCurrentTime = Calendar.getInstance();
		String tzID = "PST";
		if(user.getTimezone() != null) {
			int milisec = (int) (user.getTimezone()*60*60*1000); // convert hours to milliseconds
			tzID = TimeZone.getAvailableIDs(milisec)[0];
		}
		userCurrentTime.setTimeZone(TimeZone.getTimeZone(tzID));
		int currentHour = userCurrentTime.get(Calendar.HOUR_OF_DAY);
		int ddWindowHour = new Integer(PropertiesUtil.getConstraint("DAILY_DIGEST_HOUR"));
		if(currentHour == ddWindowHour) {
			String subject = PropertiesUtil.getMessage(EnumNotificationTypes.DAILY_DIGEST.getMessageKey());
			subject = Util.substituteSubjectValues(subject, user, null);
			
			MailUtil.sendEmail(user.getEmail(), subject, message);
		}
		
	}

	private void sendNotification(User sender, Object recipients, Activity activity, EnumNotificationTypes type) throws Exception {
		sendNotification(sender, recipients, activity, type, null, true);
	}
	
	/**
	 * 
	 * @param sender
	 * @param recipients
	 * @param activity
	 * @param type
	 * @param req - join request object
	 * @param doNotify - when false, emails and push notifications will not be sent.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void sendNotification(User sender, Object recipients, Activity activity, EnumNotificationTypes type, JoinRequest req, boolean doNotify, Object...args) throws Exception {
		String subject = PropertiesUtil.getMessage(type.getMessageKey());
		subject = Util.substituteSubjectValues(subject, sender, activity);

		Notification notification = new Notification();
		notification.setRecipients(new HashSet<NotificationRecipient>());
		
		Map<String, String> extEmailVars = new HashMap<String, String>(); 
		if(type.equals(EnumNotificationTypes.NEW_DM)) {
			extEmailVars.put("-message-", args[0].toString());
			notification.setData(args[1].toString());
		}
		
		if (recipients instanceof User) {
			User rec = (User) recipients;
			boolean isBlocked = spikeDAO.isBlockedUser(sender.getId(), rec);
			if(isBlocked) return; // sender is blocked by recipient
			notification.getRecipients().add(new NotificationRecipient(notification, (User) recipients));
			List<User> recList = new ArrayList<User>();
			recList.add(rec);
			if(doNotify)
				this.push(recList, type, sender, activity, subject, req, extEmailVars);
		} else if (recipients instanceof Collection) {
			for (User rec : (Collection<User>) recipients) {
				boolean isBlocked = spikeDAO.isBlockedUser(sender.getId(), rec);
				if(isBlocked) continue; // sender is blocked by recipient
				notification.getRecipients().add(new NotificationRecipient(notification, (User) rec));
			}
			if(doNotify)
				this.push((Collection<User>) recipients, type, sender, activity, subject, req, extEmailVars);
		}
		notification.setSender(sender);
		if(activity != null) {
			notification.setActivity_id(activity.getId());
			notification.setIcon(activity.getType().getIcon_url());
		}
		notification.setText(subject);
		notification.setUnread(true);
		notification.setType(type.toString());
		
		spikeDAO.save(notification);
	}
	
	private void push(Collection<User> recipients, EnumNotificationTypes type, User sender, final Activity activity, final String subject, final JoinRequest req, Map<String, String> extEmailVars) throws Exception {
		final List<Device> iosDevices = new ArrayList<Device>();
		final List<String> androidDevices = new ArrayList<String>();
		final List<Email> emails = new ArrayList<Email>();
		for (User recipient : recipients) {
			boolean pushOnDevice = !EnumUserPref.getByName(type.getUserPref()).getPushDisabled();
			boolean pushEmail = true;

			List<UserPreferences> prefs = spikeDAO.fetchUserPrefs(recipient.getId());
			int index = prefs.indexOf(new UserPreferences(type.getUserPref(), recipient));
			if(index >= 0) {
				pushOnDevice = prefs.get(index).getPushNotification();
				pushEmail = prefs.get(index).getEmailNotification();
			} 

			if(pushOnDevice) {
				List<Device> devices = spikeDAO.fetchUserDevices(recipient.getId());
				for (Device device : devices) {
					if(device.getDevice_type().equals("ios")) {
						device.setUnreadCount(spikeDAO.fetchNotificationCount(device.getUser().getId()));
						iosDevices.add(device);
					}
					if(device.getDevice_type().equals("android")) {
						androidDevices.add(device.getDevice_id());
					}
				}
			}
			if(pushEmail) {
				String emailTxt = Util.getTemplate(type.getMessageKey() + ".html");
				emailTxt = Util.substituteValues(emailTxt, sender, activity, null);
				if(req != null)
					emailTxt = emailTxt.replace("-joinerID", req.getUser().getId().toString());
				if(!Util.isEmpty(extEmailVars.keySet())) {
					for (String key : extEmailVars.keySet()) {
						emailTxt = emailTxt.replace(key, extEmailVars.get(key));
					}
				}

				boolean production = new Boolean(PropertiesUtil.getConstraint("production"));
				if(production)
					emails.add(new Email(recipient.getEmail(), subject, emailTxt));
			}
		}
		
		threadPool.execute(new Runnable() {
			public void run() {
				try {
					if(emails.size() > 0) {
						MailUtil.sendEmails(emails);
					}
					if(iosDevices.size() > 0) {
						String link = activity == null ? null : activity.getiOSlink();
						sendAPN(subject, link, iosDevices, req);
					}
					if(androidDevices.size() > 0) {
						String link = activity == null ? null : activity.getLink();
						sendGCM(subject, link, androidDevices, req);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void sendGCM(String subject, String link, List<String> androidDevices, JoinRequest req) throws IOException {

		Sender sender = new Sender(PropertiesUtil.getConstraint("ANDROID_API_KEY"));
		Message message;
		if(req != null)
			message = new Message.Builder().collapseKey("collapse").timeToLive(3).delayWhileIdle(true).addData("link", link).addData("text", subject).addData("joinerID", req.getUser().getId().toString()).build();
		else
			message = new Message.Builder().collapseKey("collapse").timeToLive(3).delayWhileIdle(true).addData("link", link).addData("text", subject).build();
		MulticastResult multicastResult;
		try {
			multicastResult = sender.send(message, androidDevices, 50);
		} catch (IOException e) {
			logger.error("Error posting messages", e);
			return;
		}
		List<Result> results = multicastResult.getResults();
		// analyze the results
		for (int i = 0; i < androidDevices.size(); i++) {
			String regId = androidDevices.get(i);
			Result result = results.get(i);
			String messageId = result.getMessageId();
			if (messageId != null) {
				logger.info("Succesfully sent message to android device: " + regId +
						"; messageId = " + messageId);
				String canonicalRegId = result.getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					// same device has more than on registration id: update it
					logger.info("canonicalRegId " + canonicalRegId);
				}
			} else {
				String error = result.getErrorCodeName();
				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
					// application has been removed from device - unregister it
					logger.info("Unregistered device: " + regId);
				} else {
					logger.info("Error sending message to " + regId + ": " + error);
				}
			}
		}
	}

	private void sendAPN(String subject, String iosLink, List<Device> iosDevices, JoinRequest req) {
		try {
			for (Device device : iosDevices) {
				PushNotificationPayload payload = PushNotificationPayload.complex();
				payload.addCustomAlertBody(subject);
				payload.addSound("default");
				payload.addCustomDictionary("link", iosLink);
				if(req != null) {
					payload.addCustomAlertActionLocKey("View Request");
					payload.addCustomDictionary("joinerID", req.getUser().getId().toString());
				} else {
					payload.addCustomAlertActionLocKey("View Game");
				}
				payload.addBadge(device.getUnreadCount());
				PushedNotifications pn = Push.payload(payload, PropertiesUtil.getPath("APNS_cert"), "1234", true, device.getDevice_id());
				if(pn.getSuccessfulNotifications().size() > 0)
					logger.info("Succesfully sent message to ios device: " + pn.getSuccessfulNotifications());
				if(pn.getFailedNotifications().size() > 0)
					logger.error("Could not sen message to ios device: " + pn.getFailedNotifications());
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (KeystoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendEmail(User sender, String to, Activity activity, EnumNotificationTypes type, String message) throws Exception {
		String subject = PropertiesUtil.getMessage(type.getMessageKey());
		String emailTxt = Util.getTemplate(type.getMessageKey() + ".html");
		subject = Util.substituteSubjectValues(subject, sender, activity);
		emailTxt = Util.substituteValues(emailTxt, sender, activity, message);
		MailUtil.sendEmail(to, subject, emailTxt);
	}

	private Email buildEmail(User sender, String to, Activity activity, EnumNotificationTypes type, String message) throws Exception {
		String subject = PropertiesUtil.getMessage(type.getMessageKey());
		String emailTxt = Util.getTemplate(type.getMessageKey() + ".html");
		subject = Util.substituteSubjectValues(subject, sender, activity);
		emailTxt = Util.substituteValues(emailTxt, sender, activity, message);
		Email email = new Email(to, subject, emailTxt);
		return email;
	}
	
	@SuppressWarnings("unchecked")
	public void postToFacebook(final Activity activity, final User user, final String userAuth) throws Exception {
		final List<UserPreferences> prefs = (List<UserPreferences>) fetchUserPrefs(user).get("social");
		UserPreferences p = new UserPreferences(EnumUserPref.fb_publish_on_timeline_organize.toString(), user);
		Boolean publishAction = new Boolean(prefs.get(prefs.indexOf(p)).getValue());

		Util.postToFacebook(activity, userAuth, publishAction);
	}

	public boolean isValidUser(String email) {
		return spikeDAO.isValidUser(email);
	}

	public List<DirectMessageThread> fetchDirectMessageThreads(String userAuth, String authProvider, int page) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.fetchDirectMessageThreads(user.getId(), page);
	}

	public DirectMessageThread fetchDirectMessageThread(Long id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.fetchDirectMessageThread(id, user.getId());
	}

	public DirectMessageThread newDirectMessage(String recipientId, String text, String userAuth, String authProvider, Long activityId) throws Exception {
		User user = getUser(userAuth, authProvider);
		Activity activity = null;
		if(activityId != null)
			activity = spikeDAO.fetchActivity(activityId);
		DirectMessage dm = new DirectMessage(user, new Date(), text, activity);
		DirectMessageThread thread = spikeDAO.newDirectMessage(recipientId, dm, activityId);
		User recipient = spikeDAO.getUserById(recipientId);
		sendNotification(user, recipient, null, EnumNotificationTypes.NEW_DM, null, true, text, thread.getId());
		return thread;
	}
	
	public DirectMessageThread fetchDirectMessageThreadByUserId(String id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.fetchDirectMessageThreadByUserId(user, id);
	}

	public String addUserInPlayerLists(String userId, Long[] pListIds, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		spikeDAO.addUserInPlayerLists(userId, pListIds, user.getId());
		return "user added to player lists";
	}

	public List<Comment> fetchComments(Long activityId, int page, String userAuth, String authProvider) throws Exception {
		return spikeDAO.fetchComments(activityId, page);
	}

	public Comment newComment(Long activityId, String text, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		Comment comment = spikeDAO.newComment(activityId, text, user);
		
		
		// Send notification to all participants
		Activity activity = spikeDAO.fetchActivity(activityId);
		
		String[] categories = {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()};
		Set<User> participants = new HashSet<User>(); 
		participants.addAll(spikeDAO.fetchActivityParticipants(activityId, Arrays.asList(categories)));
		
		List<ActivityCoHost> cohosts = spikeDAO.getActivityCoHosts(activityId);
		for (ActivityCoHost activityCoHost : cohosts) {
			participants.add(activityCoHost.getUser());
		}
		
		participants.add(activity.getInitiatedBy());
		
		participants.remove(user); // Exclude the comment writer from notification recipients
		
		sendNotification(user, participants, activity, EnumNotificationTypes.NEW_COMMENT, null, false);
		return comment;
	}

	@Transactional(readOnly = true)
	public User getUserDetail(String id, String userAuth, String authProvider) throws Exception {
		User liUser = getUser(userAuth, authProvider);
		
		User user = spikeDAO.getUserById(id);
		List<PlayerList> playerLists = spikeDAO.userInPlayerLists(user.getId(), liUser.getId());
		// TODO: temporary fix for EAGER fetching of players
		for (PlayerList playerList : playerLists) {
			playerList.setPlayers(null);
		}
		user.setInMyPlayerLists(playerLists);
		
		Integer organizedGameCount = spikeDAO.getOrganizedGameCountByUser(user.getId());
		user.setOrganizedGameCount(organizedGameCount);

		Integer joinedGameCount = spikeDAO.getJoinedGameCountByUser(user.getId());
		user.setJoinedGameCount(joinedGameCount);
		
		return user;
	}

	public Collection<UserActivity> explorePlayerLists(String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.explorePlayerLists(user.getId());
	}

	public Collection<UserActivity> explorePlayerList(Long id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.explorePlayerList(id, user.getId());
	}

	public void inviteToTeamon(final String email, String userAuth, String authProvider) throws Exception {
		final User user = getUser(userAuth, authProvider);
		threadPool.execute(new Runnable() {
			public void run() {
				try {
					sendEmail(user, email, null, EnumNotificationTypes.INVITE_TO_TEAMON, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void blockUser(String userId, String userAuth, String authProvider) throws Exception {
		User liUser = getUser(userAuth, authProvider);
		spikeDAO.blockUser(userId, liUser);
	}

	public void unblockUser(String userId, String userAuth, String authProvider) throws Exception {
		User liUser = getUser(userAuth, authProvider);
		spikeDAO.unblockUser(userId, liUser);
		
	}

	public boolean isBlockedUser(String userId, String userAuth, String authProvider) throws Exception {
		User liUser = getUser(userAuth, authProvider);
		return spikeDAO.isBlockedUser(userId, liUser);
	}

	public Subscription saveSubscription(Subscription subscription, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.saveSubscription(user, subscription);
	}

	public List<Subscription> fetchSubscriptions(String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.fetchSubscriptions(user);
	}

	public Subscription fetchSubscription(Long id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.fetchSubscription(id, user);
	}

	public boolean deleteSubscription(Long id, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.deleteSubscription(id, user);
	}

	public boolean deleteSubscriptions(List<Long> ids, String userAuth, String authProvider) throws Exception {
		User user = getUser(userAuth, authProvider);
		return spikeDAO.deleteSubscriptions(ids, user);
	}

	
}
