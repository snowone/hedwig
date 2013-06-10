<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- Import taglibs --%>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
	<title><fmt:message key="message.import.accounts"/></title>
	<script src='js/jquery/jquery.js' type='text/javascript'></script>
	<script src='js/jquery/ui.core.js' type='text/javascript'></script>
    <script src='js/jquery/jquery.cookie.js' type='text/javascript'></script>
 	<link href='styles/styles.css' rel='stylesheet' type='text/css'>
	<script type='text/javascript'>
	$(function() {
		$(".xmenu a[href=#]").click(function() {
			document.aform.todo.value = $(this).attr("id");
			document.aform.submit();
		});
	});
	</script>
</head>
<body>
	<h1><fmt:message key="message.import.accounts"/></h1>
	<form name="aform" method="post" enctype="multipart/form-data" action="console.do">
		<input type="hidden" name="acton" value="account"/>
		<input type="hidden" name="todo"/>
		<input type="hidden" name="domain" value="<c:out value='${param.domain}'/>"/>
		<input type="hidden" name="returl" value="<c:out value='${param.returl}'/>"/>
		<!--//main-basic Start-->
		<div id="main" class="main-basic">
			<!--//xmenu Start-->
			<div class="xmenu">
				<ul>
					<li><a href="#" id="doimport"><span><fmt:message key="menu.import"/></span></a></li>
					<li><a href="#" id="display"><span><fmt:message key="menu.list"/></span></a></li>
				</ul>
			</div>
			<!--//xdetail Start-->
			<div class="xdetail">
				<table>
					<caption></caption>
					<tbody>
						<tr>
							<th style="height:100px;"><fmt:message key="message.select.import.file"/></th>
							<td>
								<input type="file" name="file"/>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<!--//xdetail End-->
		</div>
		<!--//main-basic End-->
	</form>
</body>
</html>
