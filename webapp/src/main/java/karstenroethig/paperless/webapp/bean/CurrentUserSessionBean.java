package karstenroethig.paperless.webapp.bean;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.service.impl.UserAdminServiceImpl;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserSessionBean
{
	@Autowired private UserAdminServiceImpl userService;

	private UserDto currentUser;

	public UserDto getCurrentUser()
	{
		if (currentUser == null)
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null)
				return null;

			String username = authentication.getName();

			if (StringUtils.isNotBlank(username))
				currentUser = userService.find(username);
		}

		return currentUser;
	}
}
