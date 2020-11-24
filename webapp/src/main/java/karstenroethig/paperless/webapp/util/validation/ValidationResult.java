package karstenroethig.paperless.webapp.util.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	public void addError(MessageKeyEnum messageKey)
	{
		errors.add(new ValidationMessage(ValidationState.ERROR, messageKey));
	}

	public void addError(MessageKeyEnum messageKey, String ... propertyIds)
	{
		addMessage(errors, ValidationState.ERROR, messageKey, propertyIds);
	}

	public boolean hasWarnings()
	{
		return !warnings.isEmpty();
	}

	public void addWarning(MessageKeyEnum messageKey)
	{
		warnings.add(new ValidationMessage(ValidationState.WARNING, messageKey));
	}

	public void addWarning(MessageKeyEnum messageKey, String ... propertyIds)
	{
		addMessage(warnings, ValidationState.WARNING, messageKey, propertyIds);
	}

	public boolean hasInfos()
	{
		return !infos.isEmpty();
	}

	public void addInfo(MessageKeyEnum messageKey)
	{
		infos.add(new ValidationMessage(ValidationState.INFO, messageKey));
	}

	public void addInfo(MessageKeyEnum messageKey, String ... propertyIds)
	{
		addMessage(infos, ValidationState.INFO, messageKey, propertyIds);
	}

	private void addMessage(List<ValidationMessage> list, ValidationState state, MessageKeyEnum messageKey, String ... propertyIds)
	{
		if (propertyIds != null)
		{
			Set<String> propertyIdSet = Arrays.stream(propertyIds).collect(Collectors.toSet());
			list.add(new PropertyValidationMessage(state, messageKey, propertyIdSet));
		}
		else
		{
			list.add(new ValidationMessage(state, messageKey));
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
