package com.spikenow.util;

import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends javax.mail.Authenticator {
	private static final String SMTP_AUTH_USER = "TeamOn";
    private static final String SMTP_AUTH_PWD  = "zzgetwbg";
    
    public PasswordAuthentication getPasswordAuthentication() {
        String username = SMTP_AUTH_USER;
        String password = SMTP_AUTH_PWD;
        return new PasswordAuthentication(username, password);
     }
}
