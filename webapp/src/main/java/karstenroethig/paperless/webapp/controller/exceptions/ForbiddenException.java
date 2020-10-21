package karstenroethig.paperless.webapp.controller.exceptions;

public class ForbiddenException extends Exception
{
	/** serialVersionUID. */
	private static final long serialVersionUID = -7527946531034049930L;

	public ForbiddenException()
	{
		super();
	}

	public ForbiddenException(String message)
	{
		super(message);
	}
}
