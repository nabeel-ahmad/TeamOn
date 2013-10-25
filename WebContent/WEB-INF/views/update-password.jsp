<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 

<html lang="en">
<head>
	<title>TeamOn - Change Password</title>
	
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
					<li><a href="#contact">Password</a></li>
					<li><a href="http://www.teamonapp.com/index.html">Home</a></li>
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
				
				<h1>Change Password</h1>
			
				<div id="contact_form">
					
					<div class="success">
						<p> Your TeamOn password has been changed successfully! </p>
					</div>
				
					<form:form
						commandName="password" 
						action="${pageContext.request.contextPath}/web/update-password"
						method="post">
						
						<div class="validation">
							<p> Password validation failed or some other error occured </p>
						</div>
					
						<div class="row">
							<p class="left">
								Password must be at least 6 characters and must be confirmed 
							</p>
						</div>
						
						<div class="row">
							<p class="left">
								<label for="pass1">New Password </label>
								<form:password id="pass1" path="password" minlength="6"/>
							</p>
							
						</div>
					
						<div class="row">
							<p class="left">
								<label for="pass2">Confirm Password </label>
								<form:password id="pass2" path="password2" minlength="6"/>
							</p>
							
						</div>
						
   						<form:hidden path="access_token"/>     
						<input type="submit" class="button white" value="Change Password &#x2192;" onclick="return matchPasswords();"/>
						
						<script>
					    	function matchPasswords() {
					        	p1 = document.getElementById("pass1").value;
					        	p2 = document.getElementById("pass2").value;
					        	if(p1.length < 6) {
					            	alert("Error: Password must be at least 6 characters long");
					            	return false;
					        	}
					        	if(p1 != p2) {
					            	alert("Error: New and Confirm Passwords do not match.");
					            	return false;
					            };
					        	return true;
					    	}
					    </script>
					</form:form>
				
				</div>
				
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