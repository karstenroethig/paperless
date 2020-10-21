package karstenroethig.paperless.webapp.service.exceptions;

import lombok.Getter;

public class AlreadyExistsException extends Exception
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 4562763146823550696L;

	@Getter
	private final String fieldId;

	public AlreadyExistsException(String fieldId)
	{
		super();
		this.fieldId = fieldId;
	}
}
