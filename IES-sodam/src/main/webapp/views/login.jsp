<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>login</title>
<script
	src="https://cdn.jsdelivr.net/jquery.validation/1.16.0/jquery.validate.min.js"></script>
<script>
$(function(){
	$('form[id="loginForm"]').validate({
		rules : {
			emailId : 'required',
			password : 'requred',
		},
		messages:{
			emailId: 'please enter Email',
			password: 'please enter Password',
		},
		submitHandler:function(form){
			form.submit();
		}
	});
});
</script>
</head>
<body>
	<h2>Login</h2>
	<form:form action="/login" modelAttribute=accModel id="loginForm">
		<table>
			<tr>
				<td>Email:</td>
				<td><form:input path="emailId" /></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td><form:password path="password" /></td>
			</tr>
			<tr>
				<td><input type="submit" value="login" /></td>
			</tr>
		</table>
	</form:form>
	<a href="#">Forgot Password</a>
</body>
</html>