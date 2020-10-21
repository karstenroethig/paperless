package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;

public class DocumentBoxFormatter extends AbstractIdFormatter<DocumentBoxDto>
{
	@Override
	protected DocumentBoxDto createInstance()
	{
		return new DocumentBoxDto();
	}
}
