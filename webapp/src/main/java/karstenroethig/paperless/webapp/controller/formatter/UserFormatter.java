package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.UserDto;

public class UserFormatter extends AbstractIdFormatter<UserDto>
{
	@Override
	protected UserDto createInstance()
	{
		return new UserDto();
	}
}
