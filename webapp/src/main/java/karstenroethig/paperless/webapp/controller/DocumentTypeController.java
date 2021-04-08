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

import karstenroethig.paperless.webapp.bean.DocumentTypeSearchBean;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.DocumentType_;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;
import karstenroethig.paperless.webapp.model.dto.search.DocumentTypeSearchDto;
import karstenroethig.paperless.webapp.service.impl.DocumentTypeServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_DOCUMENT_TYPE)
public class DocumentTypeController extends AbstractController
{
	@Autowired private DocumentTypeServiceImpl documentTypeService;

	@Autowired private DocumentTypeSearchBean documentTypeSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = DocumentType_.NAME) Pageable pageable)
	{
		Page<DocumentTypeDto> resultsPage = documentTypeService.findBySearchParams(documentTypeSearchBean.getDocumentTypeSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, documentTypeSearchBean.getDocumentTypeSearchDto());

		return ViewEnum.DOCUMENT_TYPE_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) DocumentTypeSearchDto documentTypeSearchDto, Model model)
	{
		documentTypeSearchBean.setDocumentTypeSearchDto(documentTypeSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_TYPE, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		DocumentTypeDto documentType = documentTypeService.find(id);
		if (documentType == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.DOCUMENT_TYPE, documentType);
		return ViewEnum.DOCUMENT_TYPE_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.DOCUMENT_TYPE, documentTypeService.create());
		return ViewEnum.DOCUMENT_TYPE_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		DocumentTypeDto documentType = documentTypeService.find(id);
		if (documentType == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.DOCUMENT_TYPE, documentType);
		return ViewEnum.DOCUMENT_TYPE_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		DocumentTypeDto documentType = documentTypeService.find(id);
		if (documentType == null)
			throw new NotFoundException(String.valueOf(id));

		ValidationResult validationResult = documentTypeService.validateDelete(documentType);
		if (validationResult.hasErrors())
		{
			addValidationMessagesToRedirectAttributes(MessageKeyEnum.DOCUMENT_TYPE_DELETE_INVALID, validationResult.getErrors(), redirectAttributes);
		}
		else if (documentTypeService.delete(documentType))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_TYPE_DELETE_SUCCESS, documentType.getName()));
		}
		else
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.DOCUMENT_TYPE_DELETE_ERROR, documentType.getName()));
		}

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_TYPE, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.DOCUMENT_TYPE) @Valid DocumentTypeDto documentType, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(documentType, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_TYPE_SAVE_INVALID));
			return ViewEnum.DOCUMENT_TYPE_CREATE.getViewName();
		}

		if (documentTypeService.save(documentType) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_TYPE_SAVE_SUCCESS, documentType.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_TYPE, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_TYPE_SAVE_ERROR));
		return ViewEnum.DOCUMENT_TYPE_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.DOCUMENT_TYPE) @Valid DocumentTypeDto documentType, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(documentType, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_TYPE_UPDATE_INVALID));
			return ViewEnum.DOCUMENT_TYPE_EDIT.getViewName();
		}

		if (documentTypeService.update(documentType) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_TYPE_UPDATE_SUCCESS, documentType.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_TYPE, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_TYPE_UPDATE_ERROR));
		return ViewEnum.DOCUMENT_TYPE_EDIT.getViewName();
	}

	private boolean validate(DocumentTypeDto documentType, BindingResult bindingResult)
	{
		ValidationResult validationResult = documentTypeService.validate(documentType);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Document type %s does not exist.", ex.getMessage()));
	}
}
