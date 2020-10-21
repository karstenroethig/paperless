package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.TagDto;

public class TagFormatter extends AbstractIdFormatter<TagDto>
{
	@Override
	protected TagDto createInstance()
	{
		return new TagDto();
	}
}
