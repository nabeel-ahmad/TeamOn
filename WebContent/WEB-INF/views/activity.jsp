<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html lang="en">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# teamonapp: http://ogp.me/ns/fb/teamonapp#">
	<meta property="fb:app_id" content="126986714071548" /> 
	<meta property="og:type" content="teamonapp:game" /> 
	<meta property="og:url" content="${activity.link}" /> 
	<meta property="og:title" content="${activity.name}" /> 
	<meta property="og:description" content="${activity.description}" /> 
	<meta property="og:image" content="${activity.type.icon_url}" />

	<title>TeamOn App Game Link</title>
	
	<meta charset="utf-8" />
	<meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
	
	<!-- Stylesheets -->
	<link rel="stylesheet" type="text/css" href="/stylesheets/base-password.css" />

	<link rel="stylesheet" type="text/css" href="/stylesheets/media.queries.css" />
	<link rel="stylesheet" type="text/css" href="/stylesheets/tipsy.css" />
	<link rel="stylesheet" type="text/css" href="/javascripts/fancybox/jquery.fancybox-1.3.4.css" />
	<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Nothing+You+Could+Do|Quicksand:400,700,300">
	
	<!-- Javascripts -->
	<script type="text/javascript" src="/javascripts/jquery-1.7.1.min.js"></script>
	<script type="text/javascript" src="/javascripts/html5shiv.js"></script>
	<script type="text/javascript" src="/javascripts/jquery.tipsy.js"></script>
	<script type="text/javascript" src="/javascripts/fancybox/jquery.fancybox-1.3.4.pack.js"></script>
	<script type="text/javascript" src="/javascripts/fancybox/jquery.easing-1.3.pack.js"></script>
	<script type="text/javascript" src="/javascripts/jquery.touchSwipe.js"></script>
	<script type="text/javascript" src="/javascripts/jquery.mobilemenu.js"></script>
	<script type="text/javascript" src="/javascripts/jquery.infieldlabel.js"></script>
	<script type="text/javascript" src="/javascripts/jquery.echoslider.js"></script>
	<script type="text/javascript" src="/javascripts/fluidapp-password.js"></script>
	
	<!-- Favicons -->
	<link rel="shortcut icon" href="/images/favicon.png" />
	<link rel="apple-touch-icon" href="/images/apple-touch-icon.png">
	<link rel="apple-touch-icon" sizes="72x72" href="/images/apple-touch-icon-72x72.png">
	<link rel="apple-touch-icon" sizes="114x114" href="/images/apple-touch-icon-114x114.png">
	
</head>
<body>
	<!-- Start Wrapper -->
	<div id="page_wrapper">
		
	<!-- Start Header -->
	<header>
		<div class="container">
			
			<!-- Start Navigation -->
			<nav>
				<ul>
					<li><a href="#contact">TeamOn App Game Link</a></li>
				</ul>
				<span class="arrow"></span>
			</nav>
			<!-- End Navigation -->
		</div>
	</header>
	<!-- End Header -->
	
	<section class="container">
		
		<!-- Start App Info -->
		<div id="app_info">
			<!-- Start Logo -->
			<a href="http://www.teamonapp.com/index.html" class="logo">
				<img src="/emailImages/teamon-logo.png" alt="TeamOn" />
			</a>
			<span class="tagline"> Organize | Discover | Play </span>
		</div>
		<!-- End App Info -->		
		
		<!-- Start Pages -->
		<div id="pages">
			<div class="top_shadow"></div>
			
			<!-- Start Passcode -->
			<div id="contact" class="page">
				
				 <p>TeamOn App is only available on smartphones and tablets to give you best possible user experience.</p> <br/>
				 
				 <p> If you have already downloaded TeamOn app on your smartphone or tablet, tap on following to view the game.</p>
                 
                 <table cellpadding="0" cellspacing="0" style="border-collapse: collapse; border-width: 0;" width="324">
                 	<tr>
                        <td width="75" style="padding-top:15px; padding-bottom:3px; padding-right:10px" valign="top">
                                <a href="TeamOn://gameID/${activity.id}"><img alt="iPhoneLink" src="/emailImages/ios.gif" border="0" vspace="0" hspace="0" style="display:block;"></a>
                        </td>
                        <td width="75" style="padding-top:15px; padding-bottom:3px; padding-right:10px" valign="top">
                                <a href="https://teamonapp.com/gameID=${activity.id}"><img alt="AndroidLink" src="/emailImages/android.gif" border="0" vspace="0" hspace="0" style="display:block;"></a>
                        </td>
                 
                	</tr>
                 </table>
				
				<br/> <p> To download TeamOn App, tap on the below links while viewing on your device </p> <br/>
                 
                 <table cellpadding="0" cellspacing="0" style="border-collapse: collapse; border-width: 0;" width="424">
                 	<tr>
                        <td width="100" style="padding-top:15px; padding-bottom:3px; padding-right:10px" valign="top">
                                <a href="http://itunes.com/apps/teamon"><img alt="iPhone App" src="/emailImages/apple.png" height="60" border="0" vspace="0" hspace="0" style="display:block;"></a>
                        </td>
                        <td width="100" style="padding-top:15px; padding-bottom:3px; padding-right:10px" valign="top">
                                <a href="https://play.google.com/store/apps/details?id=com.teamon"><img alt="Android App" src="/emailImages/play.png"  height="60"border="0" vspace="0" hspace="0" style="display:block;"></a>
                        </td>
                      
                	</tr>
                 </table>
				<br/>
				<br/>
				
				
			</div>
			<!-- End Change Passcode -->
			
			
			
			<div class="bottom_shadow"></div>
		</div>
		<!-- End Pages -->
		
		<div class="clear"></div>
	</section>
	
	<!-- Start Footer -->
	<footer class="container">
		<p>Active Now, Inc. &copy; 2012. All Rights Reserved.</p>
	</footer>
	<!-- End Footer -->
	
	</div>
	<!-- End Wrapper -->

</body>
</html>