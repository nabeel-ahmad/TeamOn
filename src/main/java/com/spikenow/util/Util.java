package com.spikenow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.spikenow.enums.EnumStatusTypes;
import com.spikenow.model.Activity;
import com.spikenow.model.User;

public class Util {
	
	private static DefaultFacebookClient facebookClient;
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	
	private static String twitter_key = "iGIuMEzeUs96bfqE3znSg";
	private static String twitter_secret = "rk4oCFfcRyKR48TKyLhdaCsbS13wbTOAsLSxIV2iqg";
	
	private static Logger logger = Logger.getLogger("com.spikenow.util");

	public static boolean isStatusValid(String status) {
		EnumStatusTypes[] arr = EnumStatusTypes.values();
		for (EnumStatusTypes statusType : arr) {
			if(statusType.toString().equals(status)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param userAuth
	 * @param fields
	 * @return
	 */
	@Deprecated
	public static User authenticateFBUser(String userAuth, String fields) {
		try {
			userAuth = URLEncoder.encode(userAuth, "UTF-8");
			StringBuffer request = new StringBuffer("https://graph.facebook.com/me?access_token=" + userAuth);
			if (fields != null) {
				request.append("&fields=" + fields);
			}
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(request.toString());
			HttpResponse response;
			response = httpClient.execute(get);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result= convertStreamToString(instream);
				instream.close();

				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				User user = mapper.readValue(result, User.class);
				return user;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param authProvider
	 * @param userAuth
	 * @param fields
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static User authenticateUser(String authProvider, String userAuth, String fields) throws ClientProtocolException, IOException {
			userAuth = URLEncoder.encode(userAuth, "UTF-8");
			String host = null;
			if(authProvider.equals("google.com")) {
				host = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=";
			} else if(authProvider.equals("facebook.com")) {
				host = "https://graph.facebook.com/me?access_token=";
			} else if(authProvider.equals("twitter.com")) {
				return getTwitterUser(userAuth);
			}
			if (host != null) {
				StringBuffer request = new StringBuffer(host + userAuth);
				if (fields != null) {
					request.append("&fields=" + fields);
				}
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet get = new HttpGet(request.toString());
				HttpResponse response;
				response = httpClient.execute(get);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					String result = convertStreamToString(instream);
					instream.close();

					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					User user = mapper.readValue(result, User.class);
					return user;
				}
			}
		return null;
	}

	private static User getTwitterUser(String userAuth) {
		
		OAuthService service = new ServiceBuilder()
        .provider(TwitterApi.class)
        .apiKey(twitter_key)
        .apiSecret(twitter_secret)
        .build();
		
		Token requestToken = service.getRequestToken();
		
		Verifier verifier = new Verifier(userAuth);
		Token accessToken = service.getAccessToken(requestToken, verifier);
		
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.twitter.com/1/account/verify_credentials.json");
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println(response.getBody());
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		User user = null;
		try {
			user = mapper.readValue(response.getBody(), User.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println(getTimezone("-33.86", "151.20")); 
	}

	public static User authenticateGoogleUser(String userAuth, String fields) throws IOException {
		try {
			userAuth = URLEncoder.encode(userAuth, "UTF-8");
			StringBuffer request = new StringBuffer("https://accounts.google.com/o/oauth2/token?code=" + userAuth);
			if (fields != null) {
				request.append("&fields=" + fields);
			}
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(request.toString());
			HttpResponse response;
			response = httpClient.execute(get);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result= convertStreamToString(instream);
				instream.close();

				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				User user = mapper.readValue(result, User.class);
				return user;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	  }
	
	public static void postFacebookOrganizeAction(final Activity activity, final String userAuth) {
		try {
			facebookClient = new DefaultFacebookClient(userAuth);
			Calendar endTime = Calendar.getInstance();
			endTime.setTime(activity.getDateTime());
			endTime.add(Calendar.HOUR_OF_DAY, 1);
			logger.info("Posting facebook organize action: "+ activity.getLink());
			facebookClient.publish("me/teamonapp:organize", FacebookType.class, 
					Parameter.with("game", activity.getLink()), 
					Parameter.with("end_time", endTime.getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void postFacebookPlayAction(final Activity activity, final String userAuth) {
		threadPool.execute(new Runnable() {
			public void run() {
				try {
					facebookClient = new DefaultFacebookClient(userAuth);
					facebookClient.publish("me/teamonapp:play", FacebookType.class, 
							Parameter.with("game", activity.getLink()), 
							Parameter.with("start_time", activity.getDateTime()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}});
	}
	
	public static void postToFacebook(final Activity activity, final String userAuth, final Boolean publishAction) {
		threadPool.execute(new Runnable() {
			public void run() {
				facebookClient = new DefaultFacebookClient(userAuth);

				if(publishAction) {
					postFacebookOrganizeAction(activity, userAuth);
				}

				if(activity.getPublishAt() == null) return;

				try {
					String msg = getTemplate("fb-game-created.tl");
					msg = msg.replace("[user-name]", activity.getInitiatedBy().getName());

					FacebookType publishMessageResponse = 
							facebookClient.publish(activity.getPublishAt() + "/feed", FacebookType.class,
									Parameter.with("message", msg),
									Parameter.with("name", activity.getName()),
									Parameter.with("description", activity.getDescription() == null ? "" : activity.getDescription()),
									Parameter.with("link", activity.getLink()),
									Parameter.with("picture", activity.getType().getIcon_url()));

					logger.info("facebook post response: " + publishMessageResponse); 
					if(publishMessageResponse.getId() != null) {
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}});
	}
	
	public static void tweet(String userAuth, String msg) {
		OAuthService service = new ServiceBuilder()
        .provider(TwitterApi.class)
        .apiKey(twitter_key)
        .apiSecret(twitter_secret)
        .build();
		
		Token requestToken = service.getRequestToken();
		
		Verifier verifier = new Verifier(userAuth);
		Token accessToken = service.getAccessToken(requestToken, verifier);
		
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1/statuses/update.json");
	    request.addBodyParameter("status", msg);
	    service.signRequest(accessToken, request);
	    Response r = request.send();
	    System.out.println(r.getBody());
	}

	public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String getAddress(String latitude, String longitude) {
		
		try {
			latitude = URLEncoder.encode(latitude, "UTF-8");
			longitude = URLEncoder.encode(longitude, "UTF-8");
			String request = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&latlng=" + latitude + "," + longitude;
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(request);
			HttpResponse response;
			response = httpClient.execute(get);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result= convertStreamToString(instream);
				instream.close();
				
				ObjectMapper mapper = new ObjectMapper();
				HashMap<String,Object> untyped = mapper.readValue(result, HashMap.class);
				List results = (List) untyped.get("results");
				HashMap<String,Object> result1 = (HashMap<String, Object>) results.get(0);
				return result1.get("formatted_address").toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked" })
	public static Float getTimezone(String latitude, String longitude) {
		
		try {
			latitude = URLEncoder.encode(latitude, "UTF-8");
			longitude = URLEncoder.encode(longitude, "UTF-8");
			String request = "https://maps.googleapis.com/maps/api/timezone/json?sensor=false&location=" + latitude + "," + longitude + "&timestamp=0";
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(request);
			HttpResponse response;
			response = httpClient.execute(get);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result= convertStreamToString(instream);
				instream.close();
				
				ObjectMapper mapper = new ObjectMapper();
				HashMap<String,Object> untyped = mapper.readValue(result, HashMap.class);
				if(untyped != null && untyped.get("rawOffset") != null) {
					Float sec = new Float(untyped.get("rawOffset").toString());
					return sec/(60f*60f);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String format(Date dateTime, Float timezone) {
		String tzID = "PST";
		if(timezone != null) {
			int milisec = (int) (timezone*60*60*1000); // convert hours to milliseconds
			tzID = TimeZone.getAvailableIDs(milisec)[0];
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy 'at' hh:mm aaa ");
		sdf.setTimeZone(TimeZone.getTimeZone(tzID));
		return sdf.format(dateTime);
	}

	public static String getTemplate(String filename) throws IOException {
			URL url = Util.class.getClassLoader().getResource("templates/" + filename);
			String template = FileUtils.readFileToString(new File(url.getFile()));
			if (filename.startsWith("email") || filename.startsWith("share")) {
				template = template.replace("[teamon-footer]", getTemplate("teamon-footer.tl"));
				template = template.replace("[teamon-pitch]", getTemplate("teamon-pitch.tl"));
				template = template.replace("[teamon-pitch-shorter-version]", getTemplate("teamon-pitch-shorter-version.tl"));
			}
			return template;
	}
	
	public static String digest(String password) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
 
        byte byteData[] = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        return sb.toString();
 
    }

	public static String substituteSubjectValues(String txt, User sender, Activity activity) {
		if(activity != null)
			txt = txt.replace("-sport-", activity.getType().getName());
		if(sender != null)
			txt = txt.replace("-senderFullname-", sender.getName());
		return txt;
	}
	
	/**
	 * 
	 * @param txt - email text
	 * @param sender (optional)
	 * @param activity (optional)
	 * @param message - Content for email participants (optional)
	 * @return
	 * @throws IOException
	 */
	public static String substituteValues(String txt, User sender, Activity activity, String message) throws IOException {
		if(activity != null) {
			txt = txt.replace("-sport-", activity.getType().getName());
			Float timezone = null;
			if(sender != null)
				timezone = sender.getTimezone();
			if(timezone == null && activity != null) {
				timezone = getTimezone(activity.getLatitude(), activity.getLongitude());
			}
			txt = txt.replace("-datetime-", Util.format(activity.getDateTime(), timezone));
			txt = txt.replace("-location-", activity.getAddress());
			txt = txt.replace("-description-", activity.getDescription() != null ? activity.getDescription() : "");
			txt = txt.replace("-sportPhotoURL-", activity.getType().getIcon_url());
			txt = txt.replace("-gameID-", activity.getId().toString());
		}
		
		if(sender != null) {
			txt = txt.replace("-senderFullname-", sender.getName());
			txt = txt.replace("-senderPhotoURL-", sender.getPicture() != null ? sender.getPicture() : "");
		}
		if (message != null) {
			txt = txt.replace("-message-", message);
		}
		return txt;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Collection list) {
		return (list == null || list.size() == 0);
	}

	public static boolean isEmpty(Object[] pListIds) {
		if(pListIds == null) return false;
		return pListIds.length == 0;
	}
	
	public static double distance(double x1, double y1, double x2, double y2)
	{
	    double lat1 = Math.PI/180.0*x1;
	    double lat2 = Math.PI/180.0*x2;
	    double dLng = Math.PI/180.0*(y2 - y1);
	    double R = 3958.760;

	    return Math.acos(Math.sin(lat1) * Math.sin(lat2) +
	                     Math.cos(lat1) * Math.cos(lat2) * Math.cos(dLng)) * R;
	}
	
}
