package com.spikenow.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javapns.Push;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.spikenow.dao.TeamOnException;
import com.spikenow.enums.EnumServerError;
import com.spikenow.model.Activity;
import com.spikenow.model.ActivityType;
import com.spikenow.model.Comment;
import com.spikenow.model.DataList;
import com.spikenow.model.Device;
import com.spikenow.model.DirectMessageThread;
import com.spikenow.model.Image;
import com.spikenow.model.JoinRequest;
import com.spikenow.model.Notification;
import com.spikenow.model.Password;
import com.spikenow.model.PlayerList;
import com.spikenow.model.Subscription;
import com.spikenow.model.User;
import com.spikenow.model.UserActivity;
import com.spikenow.model.UserPreferences;
import com.spikenow.service.SpikeService;
import com.spikenow.util.PropertiesUtil;

/**
 * @author Nabeel
 *
 */
@Controller
public class SpikeWebController {

	@Autowired
	private SpikeService spikeService;
	
	private Logger logger = Logger.getLogger("com.spikenow.controller");
	
	@RequestMapping(value = "/activity", method = RequestMethod.POST, params={"userAuth", "authProvider"})
	public ModelAndView saveActivity(@RequestBody Activity activity, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			activity = spikeService.saveActivity(activity, userAuth, authProvider);
			if (activity != null && activity.getId() != null) {
				if(authProvider.equals("facebook.com") && activity.getPublishAt() != null) {
					spikeService.postToFacebook(activity, activity.getInitiatedBy(), userAuth);
				}
				return new ModelAndView("activity", "activity", activity);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity", method = RequestMethod.PUT, params={"userAuth", "authProvider"})
	public ModelAndView updateActivity(@RequestBody Activity activity, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			activity = spikeService.saveActivity(activity, userAuth, authProvider);
			if (activity != null && activity.getId() != null) {
				return new ModelAndView("activity", "activity", activity);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity", method = RequestMethod.GET)
	public ModelAndView fetchActivity(@RequestParam("gameID") Long id) {
		Activity activity = spikeService.getActivity(id);
		if (activity != null) {
			return new ModelAndView("activity", "activity", activity);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/next-relevant-activity", method = RequestMethod.GET)
	public ModelAndView fetchFirstUpcomingActivity(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Activity activity = spikeService.fetchFirstUpcomingActivity(userAuth, authProvider);
			if (activity != null) {
				return new ModelAndView("activity", "activity", activity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buildServerError("no upcoming activities");
	}

	@RequestMapping(value = "/activity", method = RequestMethod.GET, params={"gameID", "userAuth", "authProvider"})
	public ModelAndView fetchActivity(@RequestParam("gameID") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Activity activity = spikeService.getMyActivity(id, userAuth, authProvider);
			if (activity != null) {
				return new ModelAndView("activity", "activity", activity);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}
	
	@Deprecated
	@RequestMapping(value = "/activity/{id}", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView fetchActivityDep(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Activity activity = spikeService.getMyActivity(id, userAuth, authProvider);
			if (activity != null) {
				return new ModelAndView("activity", "activity", activity);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}


	@RequestMapping(value = "/activity/{id}/join", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView joinActivity(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		String result = spikeService.joinActivity(id, userAuth, authProvider);
		if (result != null) {
			return new ModelAndView("result", "result", result);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity/{id}/cancel", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView cancelActivity(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		String result = spikeService.cancelActivity(id, userAuth, authProvider);
		if (result != null) {
			return new ModelAndView("result", "result", result);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity/{id}/email-participants/{category}", method = RequestMethod.POST)
	public ModelAndView emailParticipants(@RequestBody String message, @PathVariable("id") Long id, @PathVariable("category") String category, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.emailParticipants(id, message, category, userAuth, authProvider);
			return new ModelAndView("result", "result", "email sent");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(value = "/join-request/{id}/respond", method = RequestMethod.GET, params={"status", "userAuth", "authProvider"})
	public ModelAndView respondTojoinRequest(@PathVariable("id") Long id, @RequestParam("status") String status, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			JoinRequest joinRequest = spikeService.respondToRequest(id, null, status, userAuth, authProvider);
			if (joinRequest != null) {
				/*if(authProvider.equals("facebook.com") && joinRequest.getStatus().equals(EnumStatusTypes.ACCEPTED.toString())) {
					spikeService.postToFacebook(joinRequest.getActivity(), "play", userAuth);
				}*/
				return new ModelAndView("result", "result", joinRequest.getStatus());
			}
		} catch (Exception e) {
			buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/join-request/{id}/respond", method = RequestMethod.GET, params={"status", "notificationId", "userAuth", "authProvider"})
	public ModelAndView respondTojoinRequest(@PathVariable("id") Long id, @RequestParam("status") String status, @RequestParam("notificationId") Long notificationId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			JoinRequest joinRequest = spikeService.respondToRequest(id, notificationId, status, userAuth, authProvider);
			if (joinRequest != null) {
				/*if(authProvider.equals("facebook.com") && joinRequest.getStatus().equals(EnumStatusTypes.ACCEPTED.toString())) {
					spikeService.postToFacebook(joinRequest.getActivity(), "play", userAuth);
				}*/
				return new ModelAndView("result", "result", joinRequest.getStatus());
			}

		} catch (Exception e) {
			buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/join-request/{id}/withdraw", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView withdrawRequest(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		String result;
		try {
			result = spikeService.withdrawRequestById(id, userAuth, authProvider);
			if (result != null) {
				return new ModelAndView("result", "result", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity/{id}/withdraw", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView withdrawRequest2(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		String result;
		try {
			result = spikeService.withdrawRequestByActivityId(id, userAuth, authProvider);
			if (result != null) {
				return new ModelAndView("result", "result", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(value = "/activity/{id}/mark-played", method = RequestMethod.GET, params={"userAuth", "authProvider"})
	public ModelAndView markPlayed(@PathVariable("id") Long activityId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		String result;
		try {
			result = spikeService.markPlayed(activityId, userAuth, authProvider);
			if (result != null) {
				return new ModelAndView("result", "result", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activities", params = {"lat", "long"})
	public ModelAndView fetchActivities(@RequestParam("lat") Double lat, @RequestParam("long") Double lng) {
		try{
			DataList<Activity> result = spikeService.fetchActivities(lat, lng, null);
			if (result != null) {
				return new ModelAndView("activities", "activities", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activities", params = {"userAuth", "authProvider", "lat", "long"})
	public ModelAndView fetchActivities(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider, @RequestParam("lat") Double lat, @RequestParam("long") Double lng) {
		DataList<Activity> result = spikeService.fetchActivities(userAuth, authProvider, lat, lng, null);
		if (result != null) {
			return new ModelAndView("activities", "activities", result);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activities", params = {"lat", "long", "radius"})
	public ModelAndView fetchActivities(@RequestParam("lat") Double lat, @RequestParam("long") Double lng, @RequestParam("radius") Double radius) {
		try{
			DataList<Activity> result = spikeService.fetchActivities(lat, lng, radius);
			if (result != null) {
				return new ModelAndView("activities", "activities", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/activities", params = {"userAuth", "authProvider", "lat", "long", "radius"})
	public ModelAndView fetchActivities(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider, @RequestParam("lat") Double lat, @RequestParam("long") Double lng, @RequestParam("radius") Double radius) {
		DataList<Activity> result = spikeService.fetchActivities(userAuth, authProvider, lat, lng, radius);
		if (result != null) {
			return new ModelAndView("activities", "activities", result);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/my-activities", params = {"userAuth", "authProvider", "page", "category"})
	public ModelAndView fetchMyActivities(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider, @RequestParam("page") Integer page, @RequestParam("category") String category) {
		try {
			DataList<Activity> result = spikeService.fetchMyActivities(userAuth, authProvider, page, category);
			if (result != null) {
				return new ModelAndView("activities", "activities", result);
			}
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError("error");
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/notifications")
	public ModelAndView fetchNotifications(@RequestParam("unread") Boolean unread, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		DataList<Notification> notifications = spikeService.fetchNotifications(unread, userAuth, authProvider);
		if (notifications != null) {
			return new ModelAndView("notifications", "notifications", notifications);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/notifications/unread-count")
	public ModelAndView fetchNotificationCount(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		Integer count = spikeService.fetchNotificationCount(userAuth, authProvider);
		if(count != null)
			return new ModelAndView("result", "result", count);
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activity/{id}/notifications", params = {"unread", "userAuth", "authProvider"})
	public ModelAndView fetchNotifications(@PathVariable("id") Long activityId, @RequestParam("unread") Boolean unread, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		DataList<Notification> notifications = spikeService.fetchNotifications(activityId, unread, userAuth, authProvider);
		if (notifications != null)
			return new ModelAndView("notifications", "notifications", notifications);
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activity/{id}/join-requests", params = {"userAuth", "authProvider"})
	public ModelAndView fetchJoinRequests(@PathVariable("id") Long activityId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		DataList<JoinRequest> requests = spikeService.fetchJoinRequests(activityId, userAuth, authProvider);
		if (requests != null)
			return new ModelAndView("join-requests", "join-requests", requests);
		return buildServerError();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/my-join-requests", params = {"userAuth", "authProvider", "page", "category"})
	public ModelAndView fetchMyJoinRequests1(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider, @RequestParam("page") Integer page, @RequestParam("category") String category) {
		List<JoinRequest> requests = spikeService.fetchMyJoinRequests(userAuth, authProvider, page, category);
		if (requests != null)
			return new ModelAndView("join-requests", "join-requests", requests);
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activity/{id}/participants", params = {"userAuth", "authProvider"})
	public ModelAndView fetchActivityParticipants(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		List<User> users = spikeService.fetchActivityParticipants(id, userAuth, authProvider);
		if (users != null)
			return new ModelAndView("users", "users", users);
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/notification/{id}")
	public ModelAndView fetchNotification(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		Notification notification = spikeService.fetchNotification(id, userAuth, authProvider);
			if(notification != null)
				return new ModelAndView("notification", "notification", notification);
			return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activity-types")
	public ModelAndView fetchActivityTypes() {
		DataList<ActivityType> types = spikeService.fetchActivityTypes();
		if(types != null) 
			return new ModelAndView("activity-types", "types", types);
		return buildServerError();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/user-preferences")
	public ModelAndView fetchUserPrefs(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Map<String, Object> result = spikeService.fetchUserPrefs(userAuth, authProvider);
			if (result != null)
				return new ModelAndView("user-pref", "user-pref", result);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.PUT, value="/user-preferences")
	public ModelAndView saveUserPrefs(@RequestBody UserPreferences[] map, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			String result = spikeService.saveUserPrefs(Arrays.asList(map), userAuth, authProvider);
			if (result != null)
				return new ModelAndView("result", "result", result);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/player-lists")
	public ModelAndView fetchPlayerLists(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		List<PlayerList> result = spikeService.fetchPlayerLists(userAuth, authProvider);
		if(result != null)
			return new ModelAndView("player-list", "player-list", result);
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/player-list")
	public ModelAndView deletePlayerList(@RequestBody Long[] ids, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			boolean result = spikeService.deletePlayerLists(ids, userAuth, authProvider);
			if (result)
				return new ModelAndView("result", "result",
						"Player lists deleted successfully");
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError("Invalid player list id(s)");
	}

	@RequestMapping(method=RequestMethod.GET, value="/profile")
	public ModelAndView getMyProfile(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			User result = spikeService.getMyProfile(userAuth, authProvider);
			if (result != null)
				return new ModelAndView("user", "user", result);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError("Access denied");
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/player-list")
	public ModelAndView savePlayerList(@RequestBody PlayerList playerList, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			PlayerList result = spikeService.savePlayerList(playerList, userAuth, authProvider);
			if(result != null)
				return new ModelAndView("player-list", "player-list", result);
		} catch (Exception e) {
			return buildError(e);
		}
		return null;
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/delete-account")
	public ModelAndView deleteAccount(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		Boolean result = spikeService.deleteAccount(userAuth, authProvider);
		if(result)
			return new ModelAndView("result", "result", "Account deleted");
		
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.POST, value="/sign-up")
	public ModelAndView signup(@RequestBody User user, @RequestParam("password") String password) {
		try {
			user = spikeService.signup(user, password);
			if (user != null)
				return new ModelAndView("user", "user", user);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/activate/{email}/{code}")
	public ModelAndView activateUser(@PathVariable("email") String email, @PathVariable("code") String code) {
		try {
			User user = spikeService.activateUser(email, code);
			if (user != null)
				return new ModelAndView("welcome", "user", user);
		} catch (Exception e) {
			buildError(e);
		}
		return buildServerError("User could not be verified");
	}

	@RequestMapping(method=RequestMethod.PUT, value="/update-profile")
	public ModelAndView updateProfile(@RequestBody User user, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			user = spikeService.updateProfile(user, userAuth, authProvider);
			if (user != null)
				return new ModelAndView("user", "user", user);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError();
	}

	@RequestMapping(method=RequestMethod.GET, value="/reset-password/{email}")
	public ModelAndView resetPassword(@PathVariable("email") String email) {
		try {
			spikeService.resetPassword(email);
			return new ModelAndView("result", "result", "New password has been sent to your email address");
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/update-password")
	public ModelAndView updatePassword(@ModelAttribute Password password, Model model) {
		if(spikeService.updatePassword(password))
			return new ModelAndView("update-password-success", "result", null);
		return new ModelAndView("update-password-failed", "result", null);
	}


	@RequestMapping(method=RequestMethod.GET, value="/update-password/{token}")
	public ModelAndView updatePassword(@PathVariable("token") String token) {
		try {
			User user = spikeService.getUser(token, "teamonapp.com");
			if (user != null)
				return new ModelAndView("update-password", "password", new Password(token));
			else
				return new ModelAndView("update-password-expire", "empty", null);
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/static-messages")
	public ModelAndView fetchStaticMessages() {
		Map<String,String> map = spikeService.fetchStaticMessages("ios");
		return new ModelAndView("static-messages", "static-messages", map);
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/static-messages/{platform}")
	public ModelAndView fetchStaticMessages(@PathVariable("platform") String platform) {
		Map<String,String> map = spikeService.fetchStaticMessages(platform);
		return new ModelAndView("static-messages", "static-messages", map);
	}

	@RequestMapping(method=RequestMethod.POST, value="/share-action/{action}")
	public ModelAndView registerSharingAction(@PathVariable("action") String action, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.registerSharingAction(action, userAuth, authProvider);
		} catch (Exception e) {
			return buildError(e);
		}
		return new ModelAndView("result", "result", "");
	}

	@RequestMapping(method=RequestMethod.GET, value="/sign-in")
	public ModelAndView signin(@RequestParam("email") String email, @RequestParam("password") String pwd) {
		try {
			Map<String, Object> result = spikeService.signin(email, pwd);
			return new ModelAndView("access-token", result);
		} catch (Exception e) {
			return buildServerError("Invalid email/password");
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/is-registered/{email}")
	public ModelAndView isValidUser(@PathVariable("email") String email) {
		try {
			boolean r = spikeService.isValidUser(email);
			return new ModelAndView("result", "result", r);
		} catch (Exception e) {
			return buildServerError("Invalid email");
		}
	}

	@Deprecated
	@RequestMapping(method=RequestMethod.POST, value="/sign-in")
	public ModelAndView signin(@RequestBody Device device, @RequestParam("email") String email, @RequestParam("password") String pwd) {
		try {
			Map<String, Object> result = spikeService.signin(email, pwd);
			return new ModelAndView("access-token", result);
		} catch (Exception e) {
			return buildServerError("Invalid email/password");
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/upload-picture")
	public ModelAndView processUpload(@RequestParam("file") MultipartFile file, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			User user = spikeService.saveUserPic(file, userAuth, authProvider);
			if (user != null)
				return new ModelAndView("user", "user", user);
		} catch (Exception e) {
			return buildError(e);
		}
		return buildServerError("Upload failed");	
	}

	@RequestMapping(method=RequestMethod.POST, value="/player-list/{id}/upload-picture")
	public ModelAndView playerListPicUpload(@PathVariable("id") Long plId, @RequestParam("file") MultipartFile file, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			String imgURL = spikeService.savePLPic(plId, file, userAuth, authProvider);
			return new ModelAndView("result", "result", imgURL);
		} catch (Exception e) {
			return buildError(e);
		}	
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/upload-image")
	public String webUpload(@ModelAttribute Image file, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) throws IOException {
		try {
			User user = spikeService.saveUserPic(file.getFile(), userAuth, authProvider);
			if (user != null)
				return "redirect:"+PropertiesUtil.getPath("webapp")+"/messages.html#uploaded";
			else
				return "redirect:"+PropertiesUtil.getPath("webapp")+"/messages.html#notuploaded";
		} catch (Exception e) {
			return "redirect:"+PropertiesUtil.getPath("webapp")+"/messages.html#notuploaded";
		}
	}

	@RequestMapping(method=RequestMethod.POST, value="/upload-image-lversion")
	public String webUploadLversion(@ModelAttribute Image file, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) throws IOException {
		try {
			User user = spikeService.saveUserPic(file.getFile(), userAuth, authProvider);
			if (user != null)
				return "redirect:"+PropertiesUtil.getPath("webapp_lversion")+"/messages.html#uploaded";
			else
				return "redirect:"+PropertiesUtil.getPath("webapp_lversion")+"/messages.html#notuploaded";
		} catch (Exception e) {
			return "redirect:"+PropertiesUtil.getPath("webapp_lversion")+"/messages.html#notuploaded";
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/register-device")
	public ModelAndView registerDevice(@RequestBody Device device, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.registerDevice(device, userAuth, authProvider);
			return new ModelAndView("result", "result",	"Device registered successfully");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/unregister-device/{id}")
	public ModelAndView unregisterDevice(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.unregisterDevice(id, userAuth, authProvider);
			return new ModelAndView("result", "result",	"Device unregistered successfully");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/test-apns/{device-id}")
	public ModelAndView testAPNS(@PathVariable("device-id") String id) {
		try {
			Push.alert("Hello World", PropertiesUtil.getPath("APNS_cert"), "1234", true, id);
			return new ModelAndView("result", "result",	"Push notification sent");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/test-gcm/{device-id}")
	public ModelAndView testGCM(@PathVariable("device-id") String id) {
		try {
			Sender sender = new Sender(PropertiesUtil.getConstraint("ANDROID_API_KEY"));
			Message message = new Message.Builder()
		    .collapseKey("collapse")
		    .timeToLive(3)
		    .delayWhileIdle(true)
		    .addData("text", "TeamOn GCM test")
		    .build();
			Result result = sender.send(message, id, 5);
			return new ModelAndView("result", "result",	"Message ID: " + result.getMessageId() + "  " + "Error Code: " + result.getErrorCodeName());
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	
	
	
	@RequestMapping(method=RequestMethod.POST, value="/check-registered-user-emails")
	public ModelAndView checkRegisteredUserEmails(@RequestBody String[] emails, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Boolean[] result = spikeService.checkRegisteredUserEmails(emails, userAuth, authProvider);
			return new ModelAndView("result", "result", result);
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/direct-message-threads")
	public ModelAndView fetchDirectMessageThreads(@RequestParam("page") int page, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			List<DirectMessageThread> threads = spikeService.fetchDirectMessageThreads(userAuth, authProvider, page);
			return new ModelAndView("threads", "threads", threads);
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/direct-message-thread/{id}")
	public ModelAndView fetchDirectMessageThread(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			DirectMessageThread thread = spikeService.fetchDirectMessageThread(id, userAuth, authProvider);
			return new ModelAndView("thread", "thread", thread);
		} catch (Exception e) {
			return buildError(e);
		}
	} 

	@RequestMapping(method=RequestMethod.GET, value="/user/{id}/direct-message-thread")
	public ModelAndView fetchDirectMessageThreadByUserId(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			DirectMessageThread thread = spikeService.fetchDirectMessageThreadByUserId(id, userAuth, authProvider);
			return new ModelAndView("thread", "thread", thread);
		} catch (Exception e) {
			return buildError(e);
		}
	} 
	
	@RequestMapping(method=RequestMethod.GET, value="/user/{id}")
	public ModelAndView getUserDetail(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			User user = spikeService.getUserDetail(id, userAuth, authProvider);
			return new ModelAndView("user", "user", user);
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/user/{id}/new-direct-message")
	public ModelAndView newDirectMessage(@RequestBody String text, @PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			DirectMessageThread thread = spikeService.newDirectMessage(id, text, userAuth, authProvider, null);
			return new ModelAndView("thread", "thread", thread);
		} catch (Exception e) {
			return buildError(e);
		}
	} 

	@RequestMapping(method=RequestMethod.POST, value="/user/{id}/new-direct-message", params={"activityId", "userAuth", "authProvider"})
	public ModelAndView newDirectMessage(@RequestBody String text, @PathVariable("id") String id, @RequestParam("activityId") Long activityId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			DirectMessageThread thread = spikeService.newDirectMessage(id, text, userAuth, authProvider, activityId);
			return new ModelAndView("thread", "thread", thread);
		} catch (Exception e) {
			return buildError(e);
		}
	} 
	
	@RequestMapping(method=RequestMethod.PUT, value="/user/{id}/player-lists")
	public ModelAndView addUserInPlayerLists(@RequestBody Long[] pListIds, @PathVariable("id") String userId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			String result = spikeService.addUserInPlayerLists(userId, pListIds, userAuth, authProvider);
			return new ModelAndView("result", "result", result);
		} catch (Exception e) {
			return buildError(e);
		}
	} 
	
	@RequestMapping(method=RequestMethod.GET, value="/activity/{id}/comments")
	public ModelAndView fetchComments(@PathVariable("id") Long activityId, @RequestParam("page") int page, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			List<Comment> comments = spikeService.fetchComments(activityId, page, userAuth, authProvider);
			return new ModelAndView("comments", "comments", comments);
		} catch (Exception e) {
			return buildError(e);
		}
	} 
	
	@RequestMapping(method=RequestMethod.POST, value="/activity/{id}/new-comment")
	public ModelAndView newComment(@RequestBody String text, @PathVariable("id") Long activityId, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Comment comment = spikeService.newComment(activityId, text, userAuth, authProvider);
			return new ModelAndView("comment", "comment", comment);
		} catch (Exception e) {
			return buildError(e);
		}
	} 
	
	@RequestMapping(method=RequestMethod.GET, value="/player/{id}/resend-invitartion")
	public ModelAndView resendInvitation(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		return new ModelAndView("result", "result", "invitation sent"); // TODO
	} 

	@RequestMapping(method=RequestMethod.GET, value="/explore-player-lists")
	public ModelAndView explorePlayerLists(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Collection<UserActivity> userActivities = spikeService.explorePlayerLists(userAuth, authProvider);
			return new ModelAndView("user-activities", "userActivities", userActivities);
		} catch (Exception e) {
			return buildError(e);
		}
	} 

	@RequestMapping(method=RequestMethod.GET, value="/explore-player-list/{id}")
	public ModelAndView explorePlayerListById(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Collection<UserActivity> userActivities = spikeService.explorePlayerList(id, userAuth, authProvider);
			return new ModelAndView("user-activities", "userActivities", userActivities);
		} catch (Exception e) {
			return buildError(e);
		}
	} 

	@RequestMapping(method=RequestMethod.GET, value="/is-first-login")
	public ModelAndView isFirstLogin(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Boolean firstLogin = spikeService.getUser(userAuth, authProvider).getFirstLogin();
			return new ModelAndView("result", "result", firstLogin);
		} catch (Exception e) {
			return buildError(e);
		}
	} 

	@RequestMapping(method=RequestMethod.GET, value="/invite-to-teamon/{email}")
	public ModelAndView inviteToTeamon(@PathVariable("email") String email, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.inviteToTeamon(email, userAuth, authProvider);
			return new ModelAndView("result", "result", "invitation sent");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/user/{id}/block")
	public ModelAndView blockUser(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.blockUser(id, userAuth, authProvider);
			return new ModelAndView("result", "result", "User blocked");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/user/{id}/unblock")
	public ModelAndView unblockUser(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			spikeService.unblockUser(id, userAuth, authProvider);
			return new ModelAndView("result", "result", "User unblocked");
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/user/{id}/is-blocked")
	public ModelAndView isBlockedUser(@PathVariable("id") String id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			boolean result = spikeService.isBlockedUser(id, userAuth, authProvider);
			return new ModelAndView("result", "result", result);
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.POST, value="/subscription")
	public ModelAndView saveSubscription(@RequestBody Subscription subscription, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Subscription result = spikeService.saveSubscription(subscription, userAuth, authProvider);
			return new ModelAndView("subscription", "subscription", result);
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/subscriptions")
	public ModelAndView fetchSubscriptions(@RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			List<Subscription> result = spikeService.fetchSubscriptions(userAuth, authProvider);
			return new ModelAndView("subscriptions", "subscriptions", result);
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.GET, value="/subscription/{id}")
	public ModelAndView fetchSubscription(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			Subscription result = spikeService.fetchSubscription(id, userAuth, authProvider);
			return new ModelAndView("subscription", "subscription", result);
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/subscription/{id}")
	public ModelAndView deleteSubscription(@PathVariable("id") Long id, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			boolean result = spikeService.deleteSubscription(id, userAuth, authProvider);
			if(result)
				return new ModelAndView("result", "result", "Subscription deleted");
			else
				return buildError(new TeamOnException(EnumServerError.ACCESS_DENIED));
		} catch (Exception e) {
			return buildError(e);
		}
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/subscription")
	public ModelAndView deleteSubscriptions(@RequestParam("ids") String idsStr, @RequestParam("userAuth") String userAuth, @RequestParam("authProvider") String authProvider) {
		try {
			List<Long> ids = new ArrayList<Long>();
			StringTokenizer st = new StringTokenizer(idsStr, ",");
			while(st.hasMoreTokens()) {
				ids.add(new Long(st.nextToken()));
			}
			boolean result = spikeService.deleteSubscriptions(ids, userAuth, authProvider);
			if(result)
				return new ModelAndView("result", "result", "Subscription deleted");
			else
				return buildError(new TeamOnException(EnumServerError.ACCESS_DENIED));
		} catch (Exception e) {
			return buildError(e);
		}
	}
	
	
	
	private ModelAndView buildServerError() {
		return buildError(new TeamOnException(EnumServerError.INVALID_REQUEST), null);
	}

	private ModelAndView buildServerError(String detail) {
		return buildError(new TeamOnException(EnumServerError.INVALID_REQUEST), detail);
	}
	
	private ModelAndView buildError(Exception e) {
		return buildError(e, null);
	}
	
	private ModelAndView buildError(Exception e, String detail) {
		if(e instanceof TeamOnException) {
			TeamOnException errorType = (TeamOnException) e;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("message", errorType.getError().getMessage());
			map.put("code", errorType.getError().getCode());
			return new ModelAndView("error", "error", map);
		}
		
		logger.log(Level.FATAL, "Exception", e);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message", EnumServerError.INVALID_REQUEST.getMessage());
		map.put("code", EnumServerError.INVALID_REQUEST.getCode());
		if(e.getMessage() != null)
			map.put("exception", e.getMessage());
		if(detail != null)
			map.put("detail", detail);
		return new ModelAndView("error", "error", map);
	}
	
}