<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<fmt:setBundle basename="viewcontent"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Hedwig Mail Server</title>
	<style type="text/css">
		body {
			font-family:trebuchet ms, verdana, arial, tahoma;
			font-size:90%;
			color:#888;
			background-color:white;
		}
		input.box{
			font-size: 12px;
			color: #333333;
			border: solid 1px #999;
			width:150px;
		}
	</style>
	<script type='text/javascript'>
	function init() { 
	<c:if test="${not empty error}">
		alert("<fmt:bundle basename='errormessages'><fmt:message key='${error}' /></fmt:bundle>"); 
	</c:if>
	}
	</script>
</head>
<body onload="init();">
	<!-- Header -->
	<div style="border:1px solid black;"><img style="border:none" src="images/xdev_logo.jpg"></div>
	<form action="console.do" method="post" enctype="application/x-www-form-urlencoded">
		<input type="hidden" name="acton" value="session" />
		<input type="hidden" name="todo" value="login" />
		<input type="hidden" name="facility" value="PropertiesLogin" />
		<table style="background: #EEE; border: 1px solid #DDD; padding: 10px;" cellpadding="5" cellspacing="0">
			<tr>
				<td align="right" nowrap><fmt:message key="login.username" />:</td>
				<td><input type="text" name="username" class="box" value="" /></td>
			</tr>
			<tr>
				<td align="right" nowrap><fmt:message key="login.password" />:</td>
				<td><input type="password" name="password" class="box" value="" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td nowrap>
					<input type="submit" value="<fmt:message key='menu.login' />" name="submit" />
					&nbsp;&nbsp;&nbsp;
					<input type="button" name="cancel" value="<fmt:message key='menu.cancel' />" onclick="javascript:window.location.href='401.html'"/>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>
