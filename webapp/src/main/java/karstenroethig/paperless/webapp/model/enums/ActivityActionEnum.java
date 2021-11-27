package karstenroethig.paperless.webapp.model.enums;

import lombok.Getter;

@Getter
public enum ActivityActionEnum
{
	CONTACT_CREATED,
	CONTACT_UPDATED,
	CONTACT_DELETED,

	DOCUMENT_CREATED,
	DOCUMENT_UPDATED,
	DOCUMENT_DELETED,

	UNKNOWN;
}
