package karstenroethig.paperless.webapp.util.jaxb.adapters;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime>
{
	@Override
	public LocalDateTime unmarshal(String text) throws Exception
	{
		return LocalDateTime.parse(text);
	}

	@Override
	public String marshal(LocalDateTime value) throws Exception
	{
		if (value == null)
			return null;
		return value.toString();
	}
}
