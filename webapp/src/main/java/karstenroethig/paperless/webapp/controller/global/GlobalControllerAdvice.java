package karstenroethig.paperless.webapp.controller.global;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class GlobalControllerAdvice
{
	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
		binder.registerCustomEditor(String.class, stringTrimmerEditor);
	}
}
