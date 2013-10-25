package com.spikenow.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private  Properties properties;
	private  Properties iosDialogs, androidDialogs, webDialogs;
	private static InputStream pathStream ;
	private static InputStream stream;
	private static InputStream iosMessageStream;
	private static InputStream androidMessageStream;
	private static InputStream webMessageStream;
	private static InputStream constraintStream;
	
	private static PropertiesUtil ref;
	
	
	private PropertiesUtil() throws IOException{
		pathStream = this.getClass().getClassLoader().getResourceAsStream("paths.properties");
		stream = this.getClass().getClassLoader().getResourceAsStream("messages.properties");
		iosMessageStream = this.getClass().getClassLoader().getResourceAsStream("messages_ios.properties");
		androidMessageStream = this.getClass().getClassLoader().getResourceAsStream("messages_android.properties");
		webMessageStream = this.getClass().getClassLoader().getResourceAsStream("messages_web.properties");
		constraintStream = this.getClass().getClassLoader().getResourceAsStream("constraints.properties");
		
		properties = new Properties();
		iosDialogs = new Properties();
		androidDialogs = new Properties();
		webDialogs = new Properties();
	}
	
	public static PropertiesUtil currentInstance() throws IOException{
		if (ref==null){
			ref = new PropertiesUtil();
		}
		return ref;
	}
	
	public static String getPath(String key) throws IOException{
		return PropertiesUtil.currentInstance().getValue(key,pathStream);
	}
	
	public static String getMessage(String key) throws IOException{
		return PropertiesUtil.currentInstance().getValue(key,stream);	
	}
	public static String getConstraint(String key) throws IOException{
		return PropertiesUtil.currentInstance().getValue(key,constraintStream);	
	}

	public static Properties getAllMessages() throws IOException{
		return PropertiesUtil.currentInstance().loadPropFile(stream);	
	}

	public static Properties getIosMessages() throws IOException{
		return PropertiesUtil.currentInstance().getIosDialogs();	
	}
	public static Properties getWebMessages() throws IOException{
		return PropertiesUtil.currentInstance().getWebDialogs();	
	}
	public static Properties getAndroidMessages() throws IOException{
		return PropertiesUtil.currentInstance().getAndroidDialogs();	
	}
	
	public String getValue(String key,InputStream inputStream){
		try{
			properties.load(inputStream);		
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return ""+properties.get(key);		
	}
	
	private Properties loadPropFile(InputStream stream2) throws IOException {
		properties.load(stream2);
		return properties;
	}

	public Properties getIosDialogs() throws IOException {
		iosDialogs.load(iosMessageStream);
		return iosDialogs;
	}

	public Properties getAndroidDialogs() throws IOException {
		androidDialogs.load(androidMessageStream);
		return androidDialogs;
	}

	public Properties getWebDialogs() throws IOException {
		webDialogs.load(webMessageStream);
		return webDialogs;
	}

}
