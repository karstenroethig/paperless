package karstenroethig.paperless.webapp.util.validation;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class PropertyValidationMessage extends ValidationMessage
{
	private String propertyId;

	public PropertyValidationMessage(String propertyId, ValidationState state, MessageKeyEnum key, Object... params)
	{
		super(state, key, params);
		this.propertyId = propertyId;
	}
}
