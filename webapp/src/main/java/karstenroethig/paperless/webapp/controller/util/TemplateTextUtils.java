package karstenroethig.paperless.webapp.controller.util;

import org.apache.commons.lang3.RegExUtils;
import org.unbescape.html.HtmlEscape;

public class TemplateTextUtils
{
	public static final TemplateTextUtils INSTANCE = new TemplateTextUtils();

	private TemplateTextUtils() {}

	public String escapeHtml(String text)
	{
		String newText = HtmlEscape.escapeHtml5(text);
		return RegExUtils.replaceAll(newText, "(\r\n|\n\r|\r|\n)", "<br/>");
	}
}
