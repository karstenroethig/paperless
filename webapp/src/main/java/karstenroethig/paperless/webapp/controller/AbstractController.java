package karstenroethig.paperless.webapp.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;

import karstenroethig.paperless.webapp.controller.exceptions.ForbiddenException;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.util.validation.PropertyValidationMessage;
import karstenroethig.paperless.webapp.util.validation.ValidationMessage;

public abstract class AbstractController
{
	protected void addPagingAttributes(Model model, Page<?> page)
	{
		model.addAttribute(AttributeNames.PAGE, page);
		model.addAttribute(AttributeNames.CURRENT_ITEMS, createCurrentItemsText(page));

		Iterator<Sort.Order> sortOrders = page.getSort().iterator();
		while (sortOrders.hasNext())
		{
			Sort.Order order = sortOrders.next();
			model.addAttribute(AttributeNames.SORT_PROPERTY, order.getProperty());
			model.addAttribute(AttributeNames.SORT_DESC, order.getDirection() == Sort.Direction.DESC);
			break;
		}

		model.addAttribute(AttributeNames.AVAILABLE_PAGESIZES, Arrays.asList(10, 15, 20, 25, 50, 100));
	}

	private String createCurrentItemsText(Page<?> page)
	{
		int itemsFrom = page.getNumber() * page.getSize() + 1;
		int itemsTo = page.getNumber() * page.getSize() + page.getNumberOfElements();

		return String.format("%s-%s", itemsFrom, itemsTo);
	}

	protected void addValidationMessagesToBindingResult(List<ValidationMessage> messages, BindingResult bindingResult)
	{
		if (messages == null || messages.isEmpty())
			return;

		for (ValidationMessage message : messages)
		{
			if (message instanceof PropertyValidationMessage)
			{
				PropertyValidationMessage propertyMessage = (PropertyValidationMessage)message;

				if (propertyMessage.hasPropertyIds())
				{
					for (String propertyId : propertyMessage.getPropertyIds())
					{
						bindingResult.rejectValue(propertyId, propertyMessage.getKey().getKey());
					}
				}
			}
		}
	}

	@ExceptionHandler(ForbiddenException.class)
	void handleForbiddenException(HttpServletResponse response, ForbiddenException ex) throws IOException
	{
		response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
	}
}
