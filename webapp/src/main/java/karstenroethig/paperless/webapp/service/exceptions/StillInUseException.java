package karstenroethig.paperless.webapp.service.exceptions;

import lombok.Getter;

public class StillInUseException extends Exception
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 5348971659609585383L;

	@Getter
	private final long count;

	public StillInUseException(long count)
	{
		super();
		this.count = count;
	}
}
