package karstenroethig.paperless.webapp.util.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidationException extends RuntimeException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 4517270123825198013L;

	@Getter
	private final ValidationResult validationResult;
}
