<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title><fmt:message key="message.import.accounts"/> <fmt:message key="message.error"/></title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
 	<link href='styles/styles.css' rel='stylesheet' type='text/css'>
	<script type='text/javascript'>
	</script>
</head>
<body>
	<h1><fmt:message key="message.import.accounts"/> <fmt:message key="message.error"/></h1>
	<!--//main-basic Start-->
	<div id="main" class="main-basic">
		<!--//xmenu Start-->
		<div class="xmenu">
			<ul>
				<li><a href="<c:url value='${param.returl}'/>" id="display"><span><fmt:message key="menu.list"/></span></a></li>
			</ul>
		</div>
		<!--//xmenu End-->
		<!--//xlist Start-->
		<div class="xlist">
			<table>
				<caption></caption>
				<thead>
					<tr>
						<th scope="col"><a href="#"><fmt:message key="error.line"/></a></th>
						<th scope="col"><a href="#"><fmt:message key="error.source"/></a></th>
						<th scope="col"><a href="#"><fmt:message key="error.message"/></a></th>
						<th scope="col"><a href="#">&nbsp</a></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="error" items="${errors}" varStatus="status">
					<tr>
						<td><c:out value='${error.lineNumber}'/></td>
						<td class="text-left"><c:out value="${error.sourceLine}"/></td>
						<td class="text-right"><c:out value="${error.message}"/></td>
						<td>&nbsp;</td>
					</tr>
					</c:forEach>
					<tr>
						<td colspan="4">&nbsp;</td>
					</tr>
				</tbody>
			</table>
		</div>
		<!--//xlist End-->
	</div>
	<!--//main-basic End-->
</body>
</html>
