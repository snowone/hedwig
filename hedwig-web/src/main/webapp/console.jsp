<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
	<title>Hedwig Web Console</title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
	<script src='js/jquery/jquery.dynatree.js' type='text/javascript'></script>
 	<link href='js/jquery/skin/ui.dynatree.css' rel='stylesheet' type='text/css'>
	<style type="text/css">
		body {
			font-family:trebuchet ms, verdana, arial, tahoma;
			font-size:90%;
			color:#888;
			background-color:white;
		}
		div.footer table {
			border: dashed gray;
			border-width: 1px 1px 1px 1px;
			background-color: #ddddff;
		}
	    #tree {
			vertical-align: top;
	    	width: 250px;
	    }
	    iframe {
	    	border: 1px dotted gray;
	    }
	</style>
	<script type='text/javascript'>
	$(function() {
		$("#tree").dynatree({
			onActivate: function(dtnode) {
				// Use our custom attribute to load the new target content:
				if( dtnode.data.url )
					$("[name=contentFrame]").attr("src", dtnode.data.url);
			}
		});
		$("#logout").click(function() {
			window.location.href = "console.do?acton=session&todo=logout";
		});
	});
	</script>
</head>
<body>
	<!-- Header -->
	<div style="border:1px solid black;">
		<table width="100%">
			<tr>
				<td><img style="border:none" src="images/xdev_logo.jpg"></td>
				<td align="right"><a href="#" id="logout"><span><fmt:message key="menu.logout"/></span></a></td>
			</tr>
		</table>
	</div>

	<table>
		<colgroup>
			<col width="300px" valign="top">
			<col width="90%">
		</colgroup>
		<tr>
			<!-- Menu -->
			<td valign="top">
				<div id="tree">
					<ul style="display:none">
						<li data="url: 'welcome.html'">Welcome</li>
						<li class="folder expanded">Domains
							<ul>
								<c:forEach var="domain" items="${domains}" varStatus="status">
								<li class="folder expanded"><c:out value="${domain}"/>
									<ul>
										<li data="url: 'console.do?acton=account&todo=display&domain=<c:out value="${domain}"/>'">Accounts</li>
										<li data="url: 'console.do?acton=alias&todo=display&domain=<c:out value="${domain}"/>'">Aliases</li>
									</ul>
								</li>
								</c:forEach>
							</ul>
						</li>
						<li class="folder expanded">Utilities
							<ul>
								<li>MX-query</li>
								<li>Server sendout</li>
								<li>Diagnostics</li>
							</ul>
						</li>
					</ul>
				</div>
			</td>
			<!-- Content -->
			<td>
				<iframe src="welcome.html" name="contentFrame" width="100%" height="550" scrolling="yes" marginheight="0" marginwidth="0" frameborder="0">
					<p>Your browser does not support iframes</p>
				</iframe>
			</td>
		</tr>
	</table>
	<hr>
	<!-- Footer -->
	<div class="footer"><table width="100%"><tr><td align="center">Hedwig IMAP, SMTP Server</td></tr></table></div>
</body>
</html>
