package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.GroupDto;

public class GroupFormatter extends AbstractIdFormatter<GroupDto>
{
	@Override
	protected GroupDto createInstance()
	{
		return new GroupDto();
	}
}
