package com.spikenow.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.spikenow.enums.EnumNotificationTypes;
import com.spikenow.model.Activity;
import com.spikenow.model.Email;

public class MailUtil {
	private static final String SMTP_HOST_NAME = "smtp.sendgrid.net";
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	private static Logger logger = Logger.getLogger("com.spikenow.util");

    public static void sendEmail(String to, String subject, String body) {
    	try {
    		Properties props = new Properties();
    		props.put("mail.transport.protocol", "smtp");
    		props.put("mail.smtp.host", SMTP_HOST_NAME);
    		props.put("mail.smtp.port", 587);
    		props.put("mail.smtp.auth", "true");

    		Authenticator auth = new SMTPAuthenticator();
    		Session mailSession = Session.getDefaultInstance(props, auth);
    		// uncomment for debugging infos to stdout
    		// mailSession.setDebug(true);
    		Transport transport = mailSession.getTransport();

    		MimeMessage message = new MimeMessage(mailSession);

    		Multipart multipart = new MimeMultipart("alternative");

    		BodyPart part1 = new MimeBodyPart();
    		part1.setContent(body, "text/html");



    		multipart.addBodyPart(part1);


    		message.setContent(multipart);
    		message.setFrom(new InternetAddress("no-reply@teamonapp.com", "TeamOn"));
    		message.setSubject(subject);
    		message.addRecipient(Message.RecipientType.TO,
    				new InternetAddress(to));

    		transport.connect();
    		transport.sendMessage(message,
    				message.getRecipients(Message.RecipientType.TO));

    		transport.close();
    		logger.info("Successfully sent email to: " + to + " with subject: " + subject);
    	} catch (MessagingException e) {
    		e.printStackTrace();
    	} catch (UnsupportedEncodingException e) {
    		e.printStackTrace();
    	}
        
    }
    
    public static void sendEmail(final String to, final EnumNotificationTypes type, final Map<String, String> map) {
    	threadPool.execute(new Runnable() {
    		public void run() {
    			try {
    				String subject = PropertiesUtil.getMessage(type.getMessageKey());
    				String msg;
    				msg = Util.getTemplate(type.getMessageKey()+".html");
    				for (String key : map.keySet()) {
    					msg = msg.replace(key, map.get(key));
    				}
    				sendEmail(to, subject, msg);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	});
    }
    
    public static void main(String[] args) {
    	try {
			sendEmail("leeban1@gmail.com", "test", "test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public static void sendEmails(List<Email> emails) {
		for (Email email : emails) {
			sendEmail(email.getTo(), email.getSubject(), email.getBody());
		}
	}

	public static String buildUpcomingDigestHtml(List<Activity> upcomingActivities) throws IOException {
		if(Util.isEmpty(upcomingActivities))
			return Util.getTemplate("noUpcomingGames.html");
		
		StringBuffer result = new StringBuffer();
		for (Activity activity : upcomingActivities) {
			String item = Util.getTemplate("singleUpcomingGame.html");
			item = Util.substituteValues(item, null, activity, null);
			result.append(item);
		}
		return result.toString();
	}

	public static String buildRecentDigestHtml(List<Activity> recentActivities) throws IOException {
		if(Util.isEmpty(recentActivities))
			return Util.getTemplate("noRecentGames.html");

		StringBuffer result = new StringBuffer();
		for (Activity activity : recentActivities) {
			String item = Util.getTemplate("singleRecentGame.html");
			item = Util.substituteValues(item, null, activity, null);
			result.append(item);
		}
		return result.toString();
	}
 
}