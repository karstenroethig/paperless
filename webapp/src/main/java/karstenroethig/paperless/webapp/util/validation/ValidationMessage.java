package karstenroethig.paperless.webapp.util.validation;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationMessage
{
	private ValidationState state;
	private MessageKeyEnum key;
}
