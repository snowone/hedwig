<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page isErrorPage="true" %>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  	<title><fmt:message key="message.error"/></title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
 	<link href='styles/error.css' rel='stylesheet' type='text/css'>
	</script>
  </head>
  <body>
	<table width="730" cellpadding="0" cellspacing="0" border="0">
	<!-- Error title -->
		<tr>
			<td width="60" align="left" valign="top" rowspan="2"><img src="images/error.jpg" width="48" height="48"/></td>
			<td valign="middle" align="left" width="*">
				<h1>
					<c:choose>
						<c:when test="${not empty error.cause}"><c:out value="${error.cause.class.name}" /></c:when>
						<c:otherwise><fmt:message key="message.error" /></c:otherwise>
					</c:choose>
				</h1>
			</td>
		</tr>

		<tr>
			<!-- This row is for HTTP status code, as well as the divider-->
            <td class="errorCodeAndDivider" align="right">&nbsp;
				<div class="divider"></div>
			</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><h2><fmt:bundle basename="errormessages"><fmt:message key="${error.key}" /></fmt:bundle></h2></td>
		</tr>
		<!-- InfoBlock -->
		<tr>
			<td>&nbsp;</td>
			<td>
				<h4>
					<a href="#" onclick="$('#infoBlockID').toggle(); return false;"><img src="images/down.png" border="0" class="actionIcon"/>Stack Trace</a>
				</h4>
				<div id="infoBlockID" class="infoBlock" style="display:none;"> 
					<xmp><c:out value="${error.exceptionTrace}"/></xmp>
				</div>
			</td>
		</tr>
	</table>
  </body>
</html>
