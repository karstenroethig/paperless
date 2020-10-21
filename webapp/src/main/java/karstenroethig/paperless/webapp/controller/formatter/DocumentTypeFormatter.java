package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;

public class DocumentTypeFormatter extends AbstractIdFormatter<DocumentTypeDto>
{
	@Override
	protected DocumentTypeDto createInstance()
	{
		return new DocumentTypeDto();
	}
}
