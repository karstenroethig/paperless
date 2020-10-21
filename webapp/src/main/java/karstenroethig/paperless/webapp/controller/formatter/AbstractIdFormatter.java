package karstenroethig.paperless.webapp.controller.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import karstenroethig.paperless.webapp.model.dto.AbstractDtoId;

public abstract class AbstractIdFormatter<T extends AbstractDtoId> implements Formatter<T>
{
	@Override
	public String print(T objectWithId, Locale locale)
	{
		if (objectWithId == null || objectWithId.getId() == null)
			return StringUtils.EMPTY;
		return objectWithId.getId().toString();
	}

	@Override
	public T parse(String text, Locale locale) throws ParseException
	{
		if (StringUtils.isBlank(text) || !StringUtils.isNumeric(text))
			return null;
		T objectWithId = createInstance();
		objectWithId.setId(Long.parseLong(text));
		return objectWithId;
	}

	protected abstract T createInstance();
}
