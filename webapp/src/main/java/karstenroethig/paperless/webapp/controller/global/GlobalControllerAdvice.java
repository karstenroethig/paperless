package karstenroethig.paperless.webapp.controller.global;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.TemplateDateUtils;
import karstenroethig.paperless.webapp.controller.util.TemplateTextUtils;

@ControllerAdvice
public class GlobalControllerAdvice
{
	@ModelAttribute(AttributeNames.DATE_UTILS)
	public TemplateDateUtils getDateUtils()
	{
		return TemplateDateUtils.INSTANCE;
	}

	@ModelAttribute(AttributeNames.TEXT_UTILS)
	public TemplateTextUtils getTextUtils()
	{
		return TemplateTextUtils.INSTANCE;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
		binder.registerCustomEditor(String.class, stringTrimmerEditor);
	}
}
