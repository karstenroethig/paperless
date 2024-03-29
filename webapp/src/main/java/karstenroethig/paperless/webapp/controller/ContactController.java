package karstenroethig.paperless.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.paperless.webapp.bean.search.ContactSearchBean;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.Contact_;
import karstenroethig.paperless.webapp.model.dto.ContactDto;
import karstenroethig.paperless.webapp.model.dto.search.ContactSearchDto;
import karstenroethig.paperless.webapp.service.impl.ContactServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_CONTACT)
public class ContactController extends AbstractController
{
	@Autowired private ContactServiceImpl contactService;

	@Autowired private ContactSearchBean contactSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = Contact_.NAME) Pageable pageable)
	{
		Page<ContactDto> resultsPage = contactService.findBySearchParams(contactSearchBean.getContactSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, contactSearchBean.getContactSearchDto());

		return ViewEnum.CONTACT_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) ContactSearchDto contactSearchDto, Model model)
	{
		contactSearchBean.setContactSearchDto(contactSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_CONTACT, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		ContactDto contact = contactService.find(id);
		if (contact == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.CONTACT, contact);
		return ViewEnum.CONTACT_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.CONTACT, contactService.create());
		return ViewEnum.CONTACT_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		ContactDto contact = contactService.find(id);
		if (contact == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.CONTACT, contact);
		return ViewEnum.CONTACT_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		ContactDto contact = contactService.find(id);
		if (contact == null)
			throw new NotFoundException(String.valueOf(id));

		ValidationResult validationResult = contactService.validateDelete(contact);
		if (validationResult.hasErrors())
		{
			addValidationMessagesToRedirectAttributes(MessageKeyEnum.CONTACT_DELETE_INVALID, validationResult.getErrors(), redirectAttributes);
		}
		else if (contactService.delete(contact))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.CONTACT_DELETE_SUCCESS, contact.getName()));
		}
		else
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.CONTACT_DELETE_ERROR, contact.getName()));
		}

		return UrlMappings.redirect(UrlMappings.CONTROLLER_CONTACT, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.CONTACT) @Valid ContactDto contact, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(contact, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.CONTACT_SAVE_INVALID));
			return ViewEnum.CONTACT_CREATE.getViewName();
		}

		if (contactService.save(contact) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.CONTACT_SAVE_SUCCESS, contact.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_CONTACT, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.CONTACT_SAVE_ERROR));
		return ViewEnum.CONTACT_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.CONTACT) @Valid ContactDto contact, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(contact, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.CONTACT_UPDATE_INVALID));
			return ViewEnum.CONTACT_EDIT.getViewName();
		}

		if (contactService.update(contact) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.CONTACT_UPDATE_SUCCESS, contact.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_CONTACT, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.CONTACT_UPDATE_ERROR));
		return ViewEnum.CONTACT_EDIT.getViewName();
	}

	private boolean validate(ContactDto contact, BindingResult bindingResult)
	{
		ValidationResult validationResult = contactService.validate(contact);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Contact %s does not exist.", ex.getMessage()));
	}
}
