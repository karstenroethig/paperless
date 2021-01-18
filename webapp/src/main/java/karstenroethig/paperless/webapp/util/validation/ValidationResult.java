package karstenroethig.paperless.webapp.util.validation;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.util.StringUtils;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class ValidationResult
{
	private List<ValidationMessage> errors = new ArrayList<>();
	private List<ValidationMessage> warnings = new ArrayList<>();
	private List<ValidationMessage> infos = new ArrayList<>();

	public boolean isOk()
	{
		return errors.isEmpty();
	}

	public boolean hasErrors()
	{
		return !errors.isEmpty();
	}

	public void addError(MessageKeyEnum messageKey, Object... params)
	{
		errors.add(new ValidationMessage(ValidationState.ERROR, messageKey, params));
	}

	public void addError(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addMessage(errors, propertyId, ValidationState.ERROR, messageKey, params);
	}

	public boolean hasWarnings()
	{
		return !warnings.isEmpty();
	}

	public void addWarning(MessageKeyEnum messageKey, Object... params)
	{
		warnings.add(new ValidationMessage(ValidationState.WARNING, messageKey, params));
	}

	public void addWarning(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addMessage(warnings, propertyId, ValidationState.WARNING, messageKey, params);
	}

	public boolean hasInfos()
	{
		return !infos.isEmpty();
	}

	public void addInfo(MessageKeyEnum messageKey, Object... params)
	{
		infos.add(new ValidationMessage(ValidationState.INFO, messageKey, params));
	}

	public void addInfo(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addMessage(infos, propertyId, ValidationState.INFO, messageKey, params);
	}

	private void addMessage(List<ValidationMessage> list, String propertyId, ValidationState state, MessageKeyEnum messageKey, Object... params)
	{
		if (StringUtils.isNotBlank(propertyId))
		{
			list.add(new PropertyValidationMessage(propertyId, state, messageKey, params));
		}
		else
		{
			list.add(new ValidationMessage(state, messageKey, params));
		}
	}

	public void add(ValidationResult validationResult)
	{
		if (validationResult == null)
			return;

		errors.addAll(validationResult.getErrors());
		warnings.addAll(validationResult.getWarnings());
		infos.addAll(validationResult.getInfos());
	}
}
