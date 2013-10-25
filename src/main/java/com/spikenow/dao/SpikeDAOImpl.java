package com.spikenow.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.spikenow.enums.EnumActivityCategory;
import com.spikenow.enums.EnumServerError;
import com.spikenow.enums.EnumStatusTypes;
import com.spikenow.enums.EnumUserPref;
import com.spikenow.model.Activity;
import com.spikenow.model.ActivityCoHost;
import com.spikenow.model.ActivityPlayerList;
import com.spikenow.model.ActivityType;
import com.spikenow.model.Comment;
import com.spikenow.model.Device;
import com.spikenow.model.DirectMessage;
import com.spikenow.model.DirectMessageThread;
import com.spikenow.model.JoinRequest;
import com.spikenow.model.Notification;
import com.spikenow.model.NotificationRecipient;
import com.spikenow.model.Player;
import com.spikenow.model.PlayerList;
import com.spikenow.model.SharingAction;
import com.spikenow.model.Subscription;
import com.spikenow.model.User;
import com.spikenow.model.UserActivity;
import com.spikenow.model.UserBlock;
import com.spikenow.model.UserPlayerList;
import com.spikenow.model.UserPreferences;
import com.spikenow.util.PropertiesUtil;
import com.spikenow.util.Util;

public class SpikeDAOImpl extends HibernateDaoSupport implements SpikeDAO {
	
	private Query createQuery(String sbQuery){
		try {
			return getHibernateTemplate().getSessionFactory().getCurrentSession().createQuery(sbQuery);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void saveUser(User user) {
		try {
			getHibernateTemplate().saveOrUpdate(user);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> fetchActiveUsers() {
		try {
			return getHibernateTemplate().find("from " + User.class.getSimpleName() + " u where u.activationCode is NULL");
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Activity> listActivities(Double lat, Double lng, Double radius) throws IOException {
		try {
			Query query = createQuery("SELECT a FROM " + Activity.class.getSimpleName() + " a " +
					" WHERE a.dateTime >= :now" +
					" AND a.isPublic is :public " +
					" AND a.cancelled is :cancelled " +
					" AND ( 3959 * ACOS( COS( RADIANS(:lat) ) * COS( RADIANS( a.latitude ) ) * COS( RADIANS( a.longitude ) - RADIANS(:lng) ) + SIN( RADIANS(:lat) ) * SIN( RADIANS( a.latitude ) ) ) ) < :radius " +
					" ORDER BY a.id desc");
			
			query.setParameter("lat", lat);
			query.setParameter("lng", lng);
			query.setParameter("public", true);
			query.setParameter("cancelled", false);
			query.setParameter("radius", radius != null ? radius : new Double(PropertiesUtil.getConstraint("RADIUS_MILES")));
			query.setParameter("now", new Date());
			return query.list();
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> getSubscribers(Activity result) {
		Query query = createQuery("SELECT s FROM Subscription s " +
				" WHERE s.activityType.id = :type AND s.user.id != :hostId AND ( 3959 * ACOS( COS( RADIANS(:lat) ) * COS( RADIANS( s.latitude ) ) * COS( RADIANS( s.longitude ) - RADIANS(:lng) ) + SIN( RADIANS(:lat) ) * SIN( RADIANS( s.latitude ) ) ) ) < 20 " );
		
		query.setParameter("lat", new Double(result.getLatitude()));
		query.setParameter("lng", new Double(result.getLongitude()));
		query.setParameter("type", result.getType().getId());
		query.setParameter("hostId", result.getInitiatedBy().getId());
		return query.list();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Activity fetchFirstUpcomingActivity(String userId, boolean publicOnly) {
		StringBuffer sb = new StringBuffer("SELECT a FROM " + Activity.class.getSimpleName() + " a " +
				" WHERE a.dateTime >= :now " +
				" AND a.cancelled IS NOT TRUE ");
				if(publicOnly) {
					sb.append(" AND a.isPublic IS TRUE ");
				}
				sb.append(" AND (a.initiatedBy.id = :userId " +
				" OR a.id IN (SELECT ach.activity.id FROM "+ ActivityCoHost.class.getSimpleName() + " ach WHERE ach.user.id = :userId)" +
				" OR a.id IN (SELECT jr.activity.id FROM "+ JoinRequest.class.getSimpleName() + " jr WHERE jr.user.id = :userId AND jr.status = '"+EnumStatusTypes.ACCEPTED+"')" +
				") " +
				" ORDER BY a.dateTime asc ");
		Query query = createQuery(sb.toString());
		query.setParameter("userId", userId);
		query.setParameter("now", new Date());
		query.setMaxResults(1);
		List list = query.list();
		if(list.size() > 0)
			return (Activity) list.get(0);
		return null;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> fetchDailyUpcomingActivities(String userId) {
		Query query = createQuery("SELECT a FROM " + Activity.class.getSimpleName() + " a " +
				" WHERE a.dateTime >= :now " +
				" AND a.dateTime <= :endTime " +
				" AND a.cancelled IS NOT TRUE " +
				" AND (a.initiatedBy.id = :userId " +
				" OR a.id IN (SELECT ach.activity.id FROM "+ ActivityCoHost.class.getSimpleName() + " ach WHERE ach.user.id = :userId)" +
				" OR a.id IN (SELECT jr.activity.id FROM "+ JoinRequest.class.getSimpleName() + " jr WHERE jr.user.id = :userId AND jr.status = :jrStatus)" +
				") " +
				" ORDER BY a.dateTime asc ");
		query.setParameter("userId", userId);
		query.setParameter("now", new Date());
		Calendar endTime = Calendar.getInstance();
		endTime.add(Calendar.DAY_OF_YEAR, 1);
		query.setParameter("endTime", endTime.getTime());
		query.setParameter("jrStatus", EnumStatusTypes.ACCEPTED.toString());
		List<Activity> list = query.list();
		if(list.size() > 0)
			return list;
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> fetchDailyRecentActivities(String userId) {
		Query query = createQuery("SELECT a FROM " + Activity.class.getSimpleName() + " a " +
				" WHERE a.dateTime <= :now " +
				" AND a.dateTime >= :startTime " +
				" AND a.cancelled IS NOT TRUE " +
				" AND (a.initiatedBy.id = :userId " +
				" OR a.id IN (SELECT ach.activity.id FROM "+ ActivityCoHost.class.getSimpleName() + " ach WHERE ach.user.id = :userId)" +
				" OR a.id IN (SELECT jr.activity.id FROM "+ JoinRequest.class.getSimpleName() + " jr WHERE jr.user.id = :userId AND jr.status IN (:jrStatuses))" +
				") " +
				" ORDER BY a.dateTime asc ");
		query.setParameter("userId", userId);
		query.setParameter("now", new Date());
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.DAY_OF_YEAR, -1);
		query.setParameter("startTime", startTime.getTime());
		query.setParameterList("jrStatuses", new String[] {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()});
		List<Activity> list = query.list();
		if(list.size() > 0)
			return list;
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ActivityType> listActivityTypes() {
		try {
			return getHibernateTemplate().find("from " + ActivityType.class.getSimpleName() + " at" + 
					" order by at.id asc");
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public ActivityType getActivityType(Long id) {
		try {
			Query query = createQuery("from " + ActivityType.class.getSimpleName() + " at where at.id = " + id);
			return (ActivityType) query.uniqueResult();
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Notification> listNotifications(Boolean unread, String userId) {
		try {
			StringBuffer q = new StringBuffer("select nr from " + NotificationRecipient.class.getSimpleName() + " nr where nr.recipient.id = :userId");
			q.append(" and nr.notification.createdOn >= :stDate");
			if(unread) {
				q.append(" and nr.unread = :unread");
			}
			q.append(" order by nr.notification.id desc");
			Query query = createQuery(q.toString());
			query.setParameter("userId", userId);
			
			Calendar stDate = Calendar.getInstance();
			stDate.add(Calendar.MONTH, -1);
			query.setParameter("stDate", stDate.getTime());
			
			if(unread) {
				query.setParameter("unread", unread);
			}
			List<NotificationRecipient> result = query.list();
			List<Notification> list = new ArrayList<Notification>();
			for (NotificationRecipient nr : result) {
				Notification n = nr.getNotification();
				n.setUnread(nr.getUnread());
				list.add(n);
				nr.setUnread(false);
			}
			return list;
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Notification> listNotifications(Long activityId, Boolean unread, String userId) {
		try {
			StringBuffer q = new StringBuffer("select nr from " + NotificationRecipient.class.getSimpleName() + " nr where nr.recipient.id = :userId" +
					" and nr.notification.joinRequest.activity.id = :activityId");
			if(unread) {
				q.append(" and nr.unread = :unread");
			}
			q.append(" order by nr.notification.id desc");
			Query query = createQuery(q.toString());
			query.setParameter("userId", userId);
			query.setParameter("activityId", activityId);
			if(unread) {
				query.setParameter("unread", unread);
			}
			List<NotificationRecipient> result = query.list();
			List<Notification> list = new ArrayList<Notification>();
			for (NotificationRecipient nr : result) {
				Notification n = nr.getNotification();
				n.setUnread(nr.getUnread());
				list.add(n);
				nr.setUnread(false);
			}
			return list;
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Integer fetchNotificationCount(String userId) {
		try {
			StringBuffer q = new StringBuffer("select count(nr) from " + NotificationRecipient.class.getSimpleName() + " nr where nr.recipient.id = :userId" +
					" and nr.unread = true");
			Query query = createQuery(q.toString());
			query.setParameter("userId", userId);
			Long result = (Long) query.uniqueResult();
			return result.intValue();
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Notification fetchNotification(Long id, User user) {
		try {
			Query query = createQuery("from " + NotificationRecipient.class.getSimpleName() + " nr where nr.notification.id = " + id + " and nr.recipient.id = :userId");
			query.setParameter("userId", user.getId());
			NotificationRecipient nr = (NotificationRecipient) query.uniqueResult();
			if (nr != null) {
				nr.setUnread(false);
				return nr.getNotification();
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Activity saveActivity(Activity activity) throws Exception {
		return getHibernateTemplate().merge(activity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Activity fetchActivity(Long id) {
		try {
			List<Activity> list = (List<Activity>) getHibernateTemplate().find("from " + Activity.class.getSimpleName() + " activity " +
					" where activity.id = " + id);
			Activity activity = list.get(0);
			return activity;
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Activity fetchPublicActivity(Long id) {
		try {
			List<Activity> list = (List<Activity>) getHibernateTemplate().find("from " + Activity.class.getSimpleName() + " activity " +
					" where activity.id = " + id + 
			" and activity.isPublic is true");
			return list.get(0);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Activity fetchMyActivity(Long id, User user) throws TeamOnException {
		try {
			Query query = createQuery("select activity from " + Activity.class.getSimpleName() + " activity where activity.id = " + id);
			
			
			Activity activity = (Activity) query.uniqueResult();
			
			Boolean accessGranted = activity.getIsPublic();
			
			if(activity != null) {
				// if logged in user is host
				if (isHost(activity, user)) {
					query = createQuery("select count(jr) from "
							+ JoinRequest.class.getSimpleName()
							+ " jr where jr.activity.id = " + id);
					activity.setResponseCount(((Long) query.uniqueResult()).intValue());
					accessGranted = true;
				} 
				// if logged in user is not the host then check if a join request exists
				else {
					query = createQuery("select jr.status from "
							+ JoinRequest.class.getSimpleName()
							+ " jr where jr.activity.id = " + id + " and jr.user.id = :userId");
					query.setParameter("userId", user.getId());
					List<String> jr = query.list();
					if(jr != null && jr.size() > 0) {
						activity.setJoinRequestStatus(jr.get(jr.size()-1));
						accessGranted = true;
					} else if(!accessGranted) {
						// Check if user has been invited to the private game
						List<ActivityPlayerList> pls = fetchActivityPlayerLists(id);
						for (ActivityPlayerList activityPlayerList : pls) {
							for (Player player : activityPlayerList.getPlayerList().getPlayers()) {
								if(player.getEmail().equals(user.getEmail()))
									accessGranted = true;
							}
						}
					}
				}
				
				if(!accessGranted) 
					throw new TeamOnException(EnumServerError.ACCESS_DENIED);
				
				return accessGranted ? activity : null;
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JoinRequest> fetchJoinRequests(Long activityId, String userId) {
		try {
			Query query = createQuery("select jr from " + JoinRequest.class.getSimpleName() + " jr where jr.activity.id = :activityId " +
					" and (jr.activity.initiatedBy.id = :userId or :userId in (select ach.user.id from " + ActivityCoHost.class.getSimpleName() + " ach where ach.activity.id = :activityId))");
			query.setParameter("activityId", activityId);
			query.setParameter("userId", userId);
			return query.list();
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> fetchMyActivities(String userId, Integer page, String category) throws NumberFormatException, IOException {
			StringBuffer sb = new StringBuffer("SELECT a FROM " + Activity.class.getSimpleName() + " a ");
			sb.append(" WHERE (a.initiatedBy.id = :userId OR a.id IN ") // Where user is Host or Co-host
			.append(" (SELECT ach.activity.id FROM "+ ActivityCoHost.class.getSimpleName() + " ach WHERE ach.user.id = :userId)")
			.append(") ");
			
			if (category.equalsIgnoreCase(EnumActivityCategory.CANCELLED.toString())) {
				// Canceled tab (no time span)
				sb.append(" AND a.cancelled IS TRUE ");
			} else {
				sb.append(" AND a.cancelled IS FALSE ");
				
				if (category.equalsIgnoreCase(EnumActivityCategory.UPCOMING.toString())) {
					sb.append(" AND a.dateTime >= :now ");
				} else if(category.equalsIgnoreCase(EnumActivityCategory.OLDER.toString())) {
					sb.append(" AND a.dateTime < :now ");
				}
			}
			
			sb.append("ORDER BY a.id desc");
		
			Query query = createQuery(sb.toString());			
			query.setParameter("userId", userId);
			
			if (!category.equalsIgnoreCase(EnumActivityCategory.CANCELLED.toString())) {
				query.setParameter("now", new Date());
				Integer pageSize = new Integer(PropertiesUtil.getConstraint("PAGE_SIZE"));
				query.setFirstResult((page-1)*pageSize);
				query.setMaxResults(pageSize);
			}
			
			List<Activity> list = query.list();
			for (Activity activity : list) {
				query = createQuery("select jr from " + JoinRequest.class.getSimpleName() + " jr where jr.activity.id = " + activity.getId());
				List<JoinRequest> jr = query.list();
				for (JoinRequest joinRequest : jr) {
					joinRequest.setActivity(null);
				}
				activity.setJoinRequsets(jr);
			}
			return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JoinRequest> fetchMyJoinRequests(String userId, Integer page, String category) throws NumberFormatException, IOException {
		StringBuffer sb = new StringBuffer("FROM " + JoinRequest.class.getSimpleName() + " jr ") 
		.append(" WHERE jr.user.id = :userId ");
		
		if (category.equalsIgnoreCase(EnumActivityCategory.CANCELLED.toString())) {
			// Canceled tab (no time span)
			sb.append(" AND jr.activity.cancelled IS TRUE ");
		} else {
			sb.append(" AND jr.activity.cancelled IS FALSE ");
			
			if (category.equalsIgnoreCase(EnumActivityCategory.UPCOMING.toString())) {
				sb.append(" AND jr.activity.dateTime >= :now ");
			} else if(category.equalsIgnoreCase(EnumActivityCategory.OLDER.toString())) {
				sb.append(" AND jr.activity.dateTime < :now ");
			}
		}
		
		sb.append(" ORDER BY jr.id desc ");
			
		Query query = createQuery(sb.toString());		
		query.setParameter("userId", userId);
		
		if (!category.equalsIgnoreCase(EnumActivityCategory.CANCELLED.toString())) {
			query.setParameter("now", new Date());
			Integer pageSize = new Integer(PropertiesUtil.getConstraint("PAGE_SIZE"));
			query.setFirstResult((page-1)*pageSize);
			query.setMaxResults(pageSize);
		}
		
		List<JoinRequest> list = query.list();
		for (JoinRequest joinRequest : list) {
			query = createQuery("select count(jr.id) from " + JoinRequest.class.getSimpleName() + " jr where jr.activity.id = :activityId and jr.status in (:statuses)");
			query.setParameter("activityId", joinRequest.getActivity().getId());
			query.setParameterList("statuses", new String[] {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()});
			Integer participantCount = ((Long) query.uniqueResult()).intValue();
			joinRequest.getActivity().setParticipantCount(participantCount);
			joinRequest.getActivity().setCoHosts(this.getActivityCoHosts(joinRequest.getActivity().getId()));
		}
		return list;
	}

	@Override
	public JoinRequest saveJoinRequest(JoinRequest joinRequest) {
		try {
			JoinRequest result = getHibernateTemplate().merge(joinRequest);
			return result;
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public User mergeUser(User user) {
		return getHibernateTemplate().merge(user);		
	}

	@Override
	public void save(Notification notification) {
		try {
			getHibernateTemplate().persist(notification);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JoinRequest getJoinRequest(Long id) {
		try {
			Query query = createQuery("from " + JoinRequest.class.getSimpleName() + " jr where jr.id = " + id);
			List<JoinRequest> list = query.list();
			return (JoinRequest) list.get(list.size()-1);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JoinRequest getJoinRequest(Long activityId, String userId) {
		try {
			Query query = createQuery("from " + JoinRequest.class.getSimpleName() + " jr where jr.activity.id = " + activityId + 
					" and jr.user.id = :userId");
			query.setParameter("userId", userId);
			List<JoinRequest> list = query.list();
			if(list == null || list.size() < 1) return null;
			return (JoinRequest) list.get(list.size()-1);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> fetchActivityVisibleParticipants(Long activityId) {
		Query query = createQuery("select jr.user from " + JoinRequest.class.getSimpleName() + " jr " +
				"where jr.activity.id = :activityId " +
				"and jr.activity.participantsVisible is TRUE ");
		query.setParameter("activityId", activityId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> fetchActivityParticipants(Long activityId, List<String> categories) {
		Query query = createQuery("select jr.user from " + JoinRequest.class.getSimpleName() + " jr " +
				"where jr.activity.id = :activityId " +
				"and jr.status IN (:statuses)");
		query.setParameter("activityId", activityId);
		query.setParameterList("statuses", categories);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserPreferences> fetchUserPrefs(String userId) {
		Query query = createQuery("from " + UserPreferences.class.getSimpleName() + " up where up.user.id = :userId");
		query.setParameter("userId", userId);
		List<UserPreferences> list = query.list();
		
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Device> fetchUserDevices(String userId) {
		Query query = createQuery("from " + Device.class.getSimpleName() + " d where d.user.id = :userId");
		query.setParameter("userId", userId);
		List<Device> list = query.list();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String saveUserPrefs(String userId, List<UserPreferences> newList) {
		if(userId == null) return null;
		Query query = createQuery("from " + UserPreferences.class.getSimpleName() + " usp where usp.user.id = :userId");
		query.setParameter("userId", userId);
		List<UserPreferences> oldList = query.list();
		for (UserPreferences up : newList) {
			
			if(!EnumUserPref.getAll().contains(up.getCode())) continue;
			
			UserPreferences p = new UserPreferences(new User(userId), up.getCode(), up.getValue(), up.getPushNotification(), up.getEmailNotification());
			int index = -1;
			index = oldList.indexOf(p);
			if(index >= 0) {
				oldList.get(index).setValue(p.getValue());
				oldList.get(index).setPushNotification(p.getPushNotification());
				oldList.get(index).setEmailNotification(p.getEmailNotification());
			} else {
				getHibernateTemplate().merge(p);
			}
		}
		return "Preferences saved.";
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlayerList savePlayerList(PlayerList playerList, String userId) throws Exception {
		if(userId == null) return null;
		playerList.setUser(new User(userId));
		
		if(playerList.getPlayers() != null) {
			List<Long> playerIds = new ArrayList<Long>(); // player IDs in the player list. This will be used to delete players which are removed.
			for (Player p : playerList.getPlayers()) {
				if(p.getId() != null)
					playerIds.add(p.getId());
				p.setPlayerList(playerList);
				
				if(p.getEmail() != null && p.getEmail() != "") {
					// Find user by email 
					User user = getUserByEmail(p.getEmail());
					
					if(user == null) {
						// Create new user
						if(p.getUser() == null)
							user = new User(p.getEmail());
						else {
							user = p.getUser();
							user.setId(p.getEmail());
						}
						user.setAuthProvider("teamonapp.com");
						user.setActivationCode("temp");
						user.setEmail(p.getEmail());
						this.signup(user);
					}
					p.setType(user.getAuthProvider());
					p.setUser(user);	
					
				} else if(p.getUser() != null) {
					// For facebook users
					// Check if user is registered
					User user = getUserById(p.getUser().getId());
					// If user is not registered then add user to DB  
					if(user == null) {
						user = p.getUser();
						user.setFirstLogin(true);
						this.signup(user);
					}
					p.setUser(user);
				}
				
			}
			
			if(playerIds != null && playerList.getId() != null) {
				// Delete removed players
				StringBuffer sb = new StringBuffer("DELETE FROM Player p WHERE p.playerList.id = :playerListId ");
				if(playerIds.size() > 0)
					sb.append(" AND p.id NOT IN (:playerIds)");
				
				Query query = createQuery(sb.toString());
				query.setParameter("playerListId", playerList.getId());
				
				if(playerIds.size() > 0)
					query.setParameterList("playerIds", playerIds);
				query.executeUpdate();
			}
		}
		
		PlayerList result = getHibernateTemplate().merge(playerList);
		
		if(playerList.getSharedWithUserIds() == null) return result;
			
		StringBuffer sb = new StringBuffer("DELETE FROM UserPlayerList WHERE playerList.id = :plID ");
		if(!Util.isEmpty(playerList.getSharedWithUserIds())) {
			sb.append(" AND user.id NOT IN (:userIds) ");
		}
		Query query = createQuery(sb.toString());
		query.setParameter("plID", result.getId());
		if(!Util.isEmpty(playerList.getSharedWithUserIds())) {
			query.setParameterList("userIds", playerList.getSharedWithUserIds());
		}
		query.executeUpdate();
		
		if(!Util.isEmpty(playerList.getSharedWithUserIds())) {
			List<String> oldList = getHibernateTemplate().find("SELECT upl.user.id FROM UserPlayerList upl WHERE upl.playerList.id = ?", result.getId());
			for (String uid : playerList.getSharedWithUserIds()) {
				if(!oldList.contains(uid))
					getHibernateTemplate().persist(new UserPlayerList(new User(uid), result));
			}
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<PlayerList> fetchPlayerLists(String userId) {
		if(userId == null) return null;
		Query query = createQuery("from " + PlayerList.class.getSimpleName() + " pl where pl.user.id = :userId");
		query.setParameter("userId", userId);
		List<PlayerList> list = query.list();
		
		query = createQuery("SELECT upl.playerList FROM UserPlayerList upl WHERE upl.user.id = :userId");
		query.setParameter("userId", userId);
		List<PlayerList> sharedPLists = query.list();
		for (PlayerList pl : sharedPLists) {
			pl.setPlayers(null); //TODO: Fix Jackson views
			pl.setSharedWithUsers(null);
			pl.setIsMine(false);
		}
		list.addAll(sharedPLists);
		return list;
	}
	
	@Override
	public boolean playerListHasUser(Long plId, String userId) {
		Query query = createQuery("SELECT COUNT(p.id) FROM Player p WHERE p.user.id = ? AND p.playerList.id = ?");
		query.setParameter(0, userId);
		query.setParameter(1, plId);
		return ((Long)query.uniqueResult()) > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PlayerList> getPlayerListsByIds(List<Long> playerListIds) {
		Query query = createQuery("from " + PlayerList.class.getSimpleName() + " pl where pl.id in (:ids)");
		query.setParameterList("ids", playerListIds);
		return query.list();
	}

	@Override
	public PlayerList getPlayerList(Long plId, String userId) {
		Query query = createQuery("from " + PlayerList.class.getSimpleName() + " pl where pl.id = :id AND pl.user.id = :userId");
		query.setParameter("id", plId);
		query.setParameter("userId", userId);
		return (PlayerList) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveActivityCoHosts(Long activityId, List<String> coHostEmails, List<ActivityCoHost> existingCoHosts) {
		
		if(existingCoHosts != null && coHostEmails.size() == 0) {
			getHibernateTemplate().deleteAll(existingCoHosts);
			return;
		}

		if(coHostEmails.size() == 0) return;
		
		Activity activity = new Activity(activityId);
		
//		Lookup the co-host email address in the PK column of user table. If it exists, add that user as the co-host
//		If it doesn't exist, look up the email address in email contact column (where the primary email address of a facebook user is stored), 
//		If it exists, add that user as the co-host
		
		Query query = createQuery("FROM " + User.class.getName() + " u WHERE u.id IN (:emails)");
		query.setParameterList("emails", coHostEmails);
		List<User> users = query.list();
		
		for (User user : users) {
			coHostEmails.remove(user.getEmail());
		}
		
		if(!Util.isEmpty(coHostEmails)) {
			query = createQuery("FROM " + User.class.getName() + " u WHERE u.email IN (:emails) AND u.email != u.id");
			query.setParameterList("emails", coHostEmails);
			users.addAll(query.list());
		}
		
		for (User user : users) {
			ActivityCoHost ach = new ActivityCoHost(activity, user);
			if (existingCoHosts == null || !existingCoHosts.contains(ach)) {
				getHibernateTemplate().persist(ach);
			} else {
				getHibernateTemplate().delete(ach);
			}
			if(existingCoHosts != null)
				existingCoHosts.remove(ach);
		}
		if(existingCoHosts != null)
			getHibernateTemplate().deleteAll(existingCoHosts); // co-hosts which have been removed
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<ActivityCoHost> getActivityCoHosts(Long activityId) {
		List<ActivityCoHost> list = getHibernateTemplate().find("FROM " + ActivityCoHost.class.getSimpleName() + " ach WHERE ach.activity.id = ?", activityId);
		return list;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void signup(User user) throws Exception {
		Query query = createQuery("from " + User.class.getSimpleName() + " u where u.id = :userId");
		query.setParameter("userId", user.getId());
		List<User> tmp = query.list();
		if(Util.isEmpty(tmp)) {
			user = getHibernateTemplate().merge(user);
			PlayerList defaultPL = new PlayerList();
			defaultPL.setName("default");
			defaultPL.setUser(user);
			getHibernateTemplate().persist(defaultPL);
		}
		else if(tmp.get(0).getActivationCode() != null) {
			// Signed up but not activated
			getHibernateTemplate().merge(user);
		} else 
			throw new Exception("User already exists");
	}

	@Override
	public User activateUser(String email, String code) {
		Query query = createQuery("from " + User.class.getSimpleName() + " u where u.email = :email and u.activationCode = :code");
		query.setParameter("email", email);
		query.setParameter("code", code);
		User user = (User) query.uniqueResult();
		if (user != null) {
			user.setActivationCode(null);
			return user;
		}
		return null;
	}

	@Override
	public Map<String, Object> signin(String email, String password) {
		User user = (User) getHibernateTemplate().find("from " + User.class.getName() + 
				" user where user.activationCode is null and user.id = ? and user.password = ?", email, password).get(0);
		if(user == null) return null;
		Map<String, Object> result = new HashMap<String, Object>();
		if(user.getAccess_token() == null) {
			String token = UUID.randomUUID().toString();
			user.setAccess_token(token);
			result.put("firstLogin", true);
			result.put("access-token", token);
		} else {
			result.put("firstLogin", false);
			result.put("access-token", user.getAccess_token());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getUser(String userAuth) {
		List<User> list = getHibernateTemplate().find("from " + User.class.getName() + 
				" user where user.activationCode is null and user.access_token = ?", userAuth);
		if(list != null && list.size() > 0)
			return list.get(0);
		return null;
	}
	
	@Override
	public User getExistingUser(String userId) {
		return (User) getHibernateTemplate().find("from " + User.class.getName() + 
				" user where user.activationCode is null and user.id = ?", userId).get(0);
	}

	@Override
	public Boolean deleteAccount(User user) {
		try {
			getHibernateTemplate().delete(user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public User updateProfile(User user, User updateTo) {
		if (updateTo.getFirst_name() != null) {
			user.setFirst_name(updateTo.getFirst_name());
		}
		if (updateTo.getLast_name() != null) {
			user.setLast_name(updateTo.getLast_name());
		}
		if (updateTo.getName() != null) {
			user.setName(updateTo.getName());
		}
		if (updateTo.getGender() != null) {
			user.setGender(updateTo.getGender());
		}
		user.setAddress(updateTo.getAddress());
		user.setLatitude(updateTo.getLatitude());
		user.setLongitude(updateTo.getLongitude());
		return user;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public User getUserById(String id) {
		 List list = getHibernateTemplate().find("from " + User.class.getName() + 
				" user where user.id = ?", id);
		 if(list != null && list.size() > 0)
			 return (User) list.get(0);
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public User getUserByEmail(String email) {
		List list = getHibernateTemplate().find("from " + User.class.getName() + 
				" user where user.email = ? ORDER BY user.authProvider DESC", email);
		 if(list != null && list.size() > 0)
			 return (User) list.get(0);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUsersByEmails(List<String> emails) {
		Query query = createQuery("from " + User.class.getName() + 
				" user where user.email IN (:emails)");
		query.setParameterList("emails", emails);
		List<User> list = query.list();
		 if(list != null && list.size() > 0)
			 return list;
		return null;
	}

	@Override
	public boolean isValidUser(String email) {
		Query query = createQuery("select count(*) from " + User.class.getName() + " user where user.email = ?");
		query.setParameter(0, email);
		Long count = (Long) query.uniqueResult();
		return count > 0;
	}

	@Override
	public void registerSharingAction(String action, User user, Date date) {
		getHibernateTemplate().persist(new SharingAction(action, user, date));
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean deletePlayerLists(Long[] ids, String userId) {
		Query query = createQuery("from " + PlayerList.class.getSimpleName() + " pl where pl.id in (:ids) and pl.user.id = :userId");
		query.setParameterList("ids", ids);
		query.setParameter("userId", userId);
		List list = query.list();
		if (list.size() > 0) {
			getHibernateTemplate().deleteAll(list);
			return true;
		}
		return false;
	}

	@Override
	public void saveActivityPlayerLists(Activity activity, List<PlayerList> lists) {
		for (PlayerList pl : lists) {
			ActivityPlayerList apl = new ActivityPlayerList(activity, pl);
			getHibernateTemplate().persist(apl);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ActivityPlayerList> fetchActivityPlayerLists(Long activityId) {
		return getHibernateTemplate().find("from " + ActivityPlayerList.class.getSimpleName() + " apl where apl.activity.id = ?", activityId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JoinRequest> getJoinRequests(Long activityId) {
		return getHibernateTemplate().find("from " + JoinRequest.class.getSimpleName() + " jr where jr.activity.id = ?", activityId);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void registerDevice(Device device) {
		List<Device> list = getHibernateTemplate().find("FROM " + Device.class.getSimpleName() + " d WHERE d.device_id = ?", device.getDevice_id());
		if(list != null && list.size() > 0) {
			list.get(0).setUser(device.getUser());
		} else {
			getHibernateTemplate().merge(device);
		}
	}

	@Override
	public void unregisterDevice(Device device) {
		getHibernateTemplate().delete(device);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JoinRequest> fetchAcceptedJoinRequestsForRecentActivities() {
		Query query = createQuery("SELECT jr FROM " + JoinRequest.class.getSimpleName() + " jr " +
				" WHERE jr.activity.dateTime >= :startTime " +
				" AND jr.activity.dateTime < NOW() " +
				" AND jr.activity.cancelled IS FALSE " +
				" AND jr.status in (:statuses)");
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.DAY_OF_YEAR, -1);
		query.setParameter("startTime", startTime.getTime());
		query.setParameterList("statuses", new String[] {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()});
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean[] checkRegisteredUserEmails(String[] emails) {
		Query query = createQuery("SELECT DISTINCT(u.email) FROM " + User.class.getSimpleName() + " u " +
				" WHERE u.email IN (:emails) AND u.access_token IS NOT NULL");
		query.setParameterList("emails", emails);
		List<String> registeredEmails = query.list();
		Boolean[] result = new Boolean[emails.length];
		int i = 0;
		for (String email : emails) {
			if(registeredEmails.contains(email))
				result[i] = true;
			else
				result[i] = false;
			i++;
		}
		
		// Also check if the given array for registered user IDs 
		query = createQuery("SELECT DISTINCT(u.id) FROM " + User.class.getSimpleName() + " u " +
				" WHERE u.id IN (:emails) AND u.access_token IS NOT NULL");
		query.setParameterList("emails", emails);
		registeredEmails = query.list();
		i = 0;
		for (String email : emails) {
			if(registeredEmails.contains(email))
				result[i] = true;
			i++;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DirectMessageThread> fetchDirectMessageThreads(String userId, int page) throws Exception {
		Query query = createQuery("SELECT thread FROM " + DirectMessageThread.class.getSimpleName() + " thread " +
									" WHERE thread.recipient.id = :userId OR thread.sender.id = :userId ORDER BY thread.updatedOn DESC ");
		query.setParameter("userId", userId);
		Integer pageSize = new Integer(PropertiesUtil.getConstraint("PAGE_SIZE"));
		query.setFirstResult((page-1)*pageSize);
		query.setMaxResults(pageSize);
		List<DirectMessageThread> list = query.list();
		if(Util.isEmpty(list)) return new ArrayList<DirectMessageThread>();
		
		// Prefill all threads with latest message
		for (DirectMessageThread thread : list) {
			query = createQuery(" FROM " + DirectMessage.class.getSimpleName() + " dm WHERE dm.thread.id = ? ORDER BY dm.id DESC");
			query.setParameter(0, thread.getId());
			query.setFirstResult(0);
			query.setMaxResults(1);
			thread.setMessages(query.list());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DirectMessageThread fetchDirectMessageThread(Long id, String userId) throws TeamOnException {
		Query query = createQuery("SELECT thread FROM " + DirectMessageThread.class.getSimpleName() + " thread " +
				" WHERE thread.id = :id AND " +
				" (thread.recipient.id = :userId OR thread.sender.id = :userId) ");
		query.setParameter("id", id);
		query.setParameter("userId", userId);
		DirectMessageThread thread = (DirectMessageThread) query.uniqueResult();
		
		if(thread == null)
			throw new TeamOnException(EnumServerError.RECORD_NOT_FOUND);
		
		List<DirectMessage> messages = getHibernateTemplate().find(" FROM " + DirectMessage.class.getSimpleName() + " dm WHERE dm.thread.id = ? ORDER BY dm.id ASC", id);
		thread.setMessages(messages);
		return thread;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DirectMessageThread fetchDirectMessageThreadByUserId(User liUser, String userId) throws Exception {
		Query query = createQuery("SELECT thread FROM " + DirectMessageThread.class.getSimpleName() + " thread " +
				" WHERE (thread.recipient.id = :userId OR thread.sender.id = :userId) " +
				" AND (thread.recipient.id = :liUserId OR thread.sender.id = :liUserId)");
		query.setParameter("userId", userId);
		query.setParameter("liUserId", liUser.getId());
		DirectMessageThread thread = (DirectMessageThread) query.uniqueResult();
		
		if(thread == null)
			throw new TeamOnException(EnumServerError.RECORD_NOT_FOUND);
		
		List<DirectMessage> messages = getHibernateTemplate().find(" FROM " + DirectMessage.class.getSimpleName() + " dm WHERE dm.thread.id = ? ORDER BY dm.id ASC", thread.getId());
		thread.setMessages(messages);
		return thread;		
	}

	@SuppressWarnings("unchecked")
	@Override
	public DirectMessageThread newDirectMessage(String recipientId, DirectMessage dm, Long activityId) {
		
		// Check and fetch if a thread exists between these users
		Query query = createQuery("SELECT thread FROM " + DirectMessageThread.class.getSimpleName() + " thread " +
				" WHERE (thread.recipient.id = :recId OR thread.sender.id = :recId) " +
				" AND (thread.recipient.id = :senderId OR thread.sender.id = :senderId) ");
		query.setParameter("recId", recipientId);
		query.setParameter("senderId", dm.getSender().getId());
		DirectMessageThread thread = (DirectMessageThread) query.uniqueResult();
		
		if(thread == null) {
			// Start new thread
			thread = new DirectMessageThread(dm.getSender(), new User(recipientId));
			thread.setMessages(new ArrayList<DirectMessage>());
			getHibernateTemplate().persist(thread);
		} else {
			List<DirectMessage> messages = getHibernateTemplate().find(" FROM " + DirectMessage.class.getSimpleName() + " dm WHERE dm.thread.id = ?", thread.getId());
			thread.setMessages(messages);
		}
		thread.getMessages().add(dm);
		
		dm.setThread(thread);
		thread.setUpdatedOn(new Date());
		getHibernateTemplate().persist(dm);
		
		return thread;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PlayerList> userInPlayerLists(String userId, String loggedInUserId) {
		return getHibernateTemplate().find("SELECT p.playerList FROM " + Player.class.getSimpleName() + " p " +
											" WHERE p.user.id = ? " +
											" AND p.playerList.user.id = ? ", userId, loggedInUserId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addUserInPlayerLists(String userId, Long[] pListIds, String loggedInUserId) throws TeamOnException {
		User user = getUserById(userId);
		if(user == null) throw new TeamOnException(EnumServerError.RECORD_NOT_FOUND);
		
		
		// Delete from player lists which were not checked
		StringBuffer sb = new StringBuffer("DELETE FROM Player WHERE user.id = :userId");
		if(!Util.isEmpty(pListIds)) {
			sb.append(" AND playerList.id NOT IN (:pListIds)");
		}
		Query query = createQuery(sb.toString());
		query.setParameter("userId", userId);
		if(!Util.isEmpty(pListIds)) {
			query.setParameterList("pListIds", pListIds);
		}
		query.executeUpdate();
		
		if(Util.isEmpty(pListIds))
			return;
		
		// Only use player lists which are owned by logged in user
		query = createQuery("SELECT pl.id FROM PlayerList pl WHERE pl.id IN (:pListIds) AND pl.user.id = :loggedInUserId");
		query.setParameter("loggedInUserId", loggedInUserId);
		query.setParameterList("pListIds", pListIds);
		List<Long> myPListIds = query.list();
		
		// Check if user already exists in these player lists to avoid duplication
		query = createQuery("SELECT p.playerList.id FROM Player p " +
				" WHERE p.user.id = :userId " +
				" AND p.playerList.id IN (:pListIds) ");
		query.setParameter("userId", userId);
		query.setParameterList("pListIds", pListIds);
		List<Long> existing = query.list();
		
		myPListIds.removeAll(existing);
		
		for (Long pl : myPListIds) {
			Player p = new Player(user.getEmail(), user, new PlayerList(pl));
			getHibernateTemplate().persist(p);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Comment> fetchComments(Long activityId, int page) throws Exception {
		Query query = createQuery("FROM Comment c WHERE c.activity.id = ? ORDER BY c.id desc");
		query.setParameter(0, activityId);
		Integer pageSize = new Integer(PropertiesUtil.getConstraint("PAGE_SIZE"));
		query.setFirstResult((page-1)*pageSize);
		query.setMaxResults(pageSize);
		
		return query.list();
	}

	@Override
	public int fetchCommentCount(Long activityId) {
		Long count = (Long) getHibernateTemplate().find("SELECT count(*) FROM Comment c WHERE c.activity.id = ?", activityId).get(0);
		return count.intValue();
	}

	@Override
	public Comment newComment(Long activityId, String text, User user) {
		Comment comment = new Comment(user, new Activity(activityId), text, new Date());
		return getHibernateTemplate().merge(comment);
	}

	@Override
	public Integer getOrganizedGameCountByUser(String userId) {
		Long count = (Long) getHibernateTemplate().find("SELECT count(*) FROM Activity a WHERE a.initiatedBy.id = ? AND a.cancelled IS FALSE", userId).get(0);
		return count.intValue();
	}

	@Override
	public Integer getJoinedGameCountByUser(String userId) {
		Query query = createQuery("SELECT count(*) FROM JoinRequest jr WHERE jr.user.id = :userId AND jr.status IN (:statuses)");
		query.setParameter("userId", userId);
		query.setParameterList("statuses", new String[] {EnumStatusTypes.ACCEPTED.toString(), EnumStatusTypes.PLAYED.toString()});
		Long count = (Long) query.uniqueResult();
		return count.intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<UserActivity> explorePlayerLists(String userId) throws TeamOnException {
		List<PlayerList> list = getHibernateTemplate().find("FROM PlayerList pl WHERE pl.user.id = ?", userId);
		if(Util.isEmpty(list)) return new ArrayList<UserActivity>();
		
		Set<UserActivity> result = new HashSet<UserActivity>();
		for (PlayerList playerList : list) {
			result.addAll(buildUserActivityPairs(playerList));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<UserActivity> explorePlayerList(Long id, String userId) throws TeamOnException {
		List<PlayerList> list = getHibernateTemplate().find("FROM PlayerList pl WHERE pl.id = ? AND pl.user.id = ?", id, userId);
		if(Util.isEmpty(list)) throw new TeamOnException(EnumServerError.RECORD_NOT_FOUND);
		return buildUserActivityPairs(list.get(0));
	}

	private Collection<UserActivity> buildUserActivityPairs(PlayerList list) throws TeamOnException {
		Set<UserActivity> result = new HashSet<UserActivity>();
		for (Player player : list.getPlayers()) {
			User user = player.getUser();
			if(user == null) continue;
			Activity activity = fetchFirstUpcomingActivity(user.getId(), true);
			if(activity == null) continue;
			UserActivity userActivity = new UserActivity(user, activity);
			result.add(userActivity);
		}
		return result;
	}

	@Override
	public void blockUser(String userId, User liUser) {
		if(isBlockedUser(userId, liUser)) return;
		UserBlock userBlock = new UserBlock(new User(userId), liUser);
		getHibernateTemplate().persist(userBlock);
	}

	@Override
	public void unblockUser(String userId, User liUser) {
		Query query = createQuery("DELETE FROM UserBlock WHERE blockedUser.id = ? AND blockedBy.id = ? ");
		query.setParameter(0, userId);
		query.setParameter(1, liUser.getId());
		query.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isBlockedUser(String userId, User liUser) {
		List list = getHibernateTemplate().find("SELECT ub.id FROM UserBlock ub WHERE ub.blockedUser.id = ? AND ub.blockedBy.id = ? ", userId, liUser.getId());
		return !Util.isEmpty(list);
	}
	
	@Override
	public boolean isHost(Activity activity, User user) {
		boolean userAuthorized = activity.getInitiatedBy().getId().equals(user.getId());
		if(!userAuthorized) {
			List<ActivityCoHost> cohosts = this.getActivityCoHosts(activity.getId());
			if(cohosts == null) return false;
			ActivityCoHost ach = new ActivityCoHost(activity, user);
			userAuthorized = cohosts.contains(ach);
		}
		return userAuthorized;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Subscription saveSubscription(User user, Subscription subscription) throws Exception {
		if(subscription.getId() != null) {
			List<Subscription> list = getHibernateTemplate().find("SELECT s.id FROM Subscription s WHERE s.id = ? AND s.user.id = ?", subscription.getId(), user.getId());
			if(Util.isEmpty(list))
				throw new TeamOnException(EnumServerError.ACCESS_DENIED);
		}
		subscription.setUser(user);
		return getHibernateTemplate().merge(subscription);			
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> fetchSubscriptions(User user) {
		return getHibernateTemplate().find("FROM Subscription s WHERE s.user.id = ?", user.getId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Subscription fetchSubscription(Long id, User user) {
		List<Subscription> list = getHibernateTemplate().find("FROM Subscription s WHERE s.id = ? AND s.user.id = ?", id, user.getId());
		return list.get(0);
	}

	@Override
	public boolean deleteSubscription(Long id, User user) {
		Query query = createQuery("DELETE FROM Subscription s WHERE id = ? AND user.id = ?");
		query.setParameter(0, id);
		query.setParameter(1, user.getId());
		return query.executeUpdate() > 0;
	}

	@Override
	public boolean deleteSubscriptions(List<Long> ids, User user) {
		Query query = createQuery("DELETE FROM Subscription s WHERE id IN (:ids) AND user.id = :userId");
		query.setParameterList("ids", ids);
		query.setParameter("userId", user.getId());
		return query.executeUpdate() > 0;
	}
	
}
