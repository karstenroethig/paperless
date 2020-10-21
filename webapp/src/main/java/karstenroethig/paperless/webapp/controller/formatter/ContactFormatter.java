package karstenroethig.paperless.webapp.controller.formatter;

import karstenroethig.paperless.webapp.model.dto.ContactDto;

public class ContactFormatter extends AbstractIdFormatter<ContactDto>
{
	@Override
	protected ContactDto createInstance()
	{
		return new ContactDto();
	}
}
