package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.DocumentDto;

public class DocumentFormatter extends AbstractIdFormatter<DocumentDto>
{
	@Override
	protected DocumentDto createInstance()
	{
		return new DocumentDto();
	}
}
