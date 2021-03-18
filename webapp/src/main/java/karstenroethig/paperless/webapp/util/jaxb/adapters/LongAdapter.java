package karstenroethig.paperless.webapp.util.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LongAdapter extends XmlAdapter<String, Long>
{
	@Override
	public Long unmarshal(String text)
	{
		return Long.parseLong(text);
	}

	@Override
	public String marshal(Long value)
	{
		if (value == null)
			return null;
		return value.toString();
	}
}
