package karstenroethig.paperless.webapp.util.validation;

import java.util.Set;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class PropertyValidationMessage extends ValidationMessage
{
	private Set<String> propertyIds;

	public PropertyValidationMessage(ValidationState state, MessageKeyEnum key, Set<String> propertyIds)
	{
		super(state, key);
		this.propertyIds = propertyIds;
	}

	public boolean hasPropertyIds()
	{
		return propertyIds != null && !propertyIds.isEmpty();
	}
}
