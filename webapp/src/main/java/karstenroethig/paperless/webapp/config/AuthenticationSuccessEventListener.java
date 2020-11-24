package karstenroethig.paperless.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import karstenroethig.paperless.webapp.service.impl.UserAdminServiceImpl;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent>
{
	@Autowired UserAdminServiceImpl userService;

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event)
	{
		String username = event.getAuthentication().getName();
		userService.resetFailedLoginAttempts(username);
	}
}
