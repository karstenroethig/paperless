package karstenroethig.paperless.webapp.util.jaxb.adapters;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate>
{
	@Override
	public LocalDate unmarshal(String text) throws Exception
	{
		return LocalDate.parse(text);
	}

	@Override
	public String marshal(LocalDate value) throws Exception
	{
		if (value == null)
			return null;
		return value.toString();
	}
}
