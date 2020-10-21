package karstenroethig.paperless.webapp.controller.exceptions;

public class NotFoundException extends RuntimeException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = -4981372980250366741L;

	public NotFoundException(String message)
	{
		super(message);
	}
}
