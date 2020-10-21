package karstenroethig.paperless.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import karstenroethig.paperless.webapp.controller.formatter.ContactFormatter;
import karstenroethig.paperless.webapp.controller.formatter.DocumentBoxFormatter;
import karstenroethig.paperless.webapp.controller.formatter.DocumentFormatter;
import karstenroethig.paperless.webapp.controller.formatter.DocumentTypeFormatter;
import karstenroethig.paperless.webapp.controller.formatter.TagFormatter;

@Configuration
public class FormatterConfiguration implements WebMvcConfigurer
{
	@Override
	public void addFormatters(FormatterRegistry formatterRegistry)
	{
		formatterRegistry.addFormatter(new ContactFormatter());
		formatterRegistry.addFormatter(new DocumentFormatter());
		formatterRegistry.addFormatter(new DocumentBoxFormatter());
		formatterRegistry.addFormatter(new DocumentTypeFormatter());
		formatterRegistry.addFormatter(new TagFormatter());
	}
}
