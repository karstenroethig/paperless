package karstenroethig.paperless.webapp.controller.formatter;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;

import karstenroethig.paperless.webapp.model.dto.TagDto;

public class TagFormatter implements Formatter<TagDto>
{
	@Override
	public String print(TagDto tag, Locale locale)
	{
		if (tag == null)
			return StringUtils.EMPTY;
		if (tag.getId() != null)
			return tag.getId().toString();
		return tag.getName();
	}

	@Override
	public TagDto parse(String text, Locale locale) throws ParseException
	{
		if (StringUtils.isBlank(text))
			return null;
		TagDto tag = new TagDto();
		if (StringUtils.isNumeric(text))
			tag.setId(Long.parseLong(text));
		else
			tag.setName(text);
		return tag;
	}
}

