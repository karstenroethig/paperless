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

import karstenroethig.paperless.webapp.bean.DocumentBoxSearchBean;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.DocumentBox_;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxSearchDto;
import karstenroethig.paperless.webapp.service.exceptions.StillInUseException;
import karstenroethig.paperless.webapp.service.impl.DocumentBoxServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_DOCUMENT_BOX)
public class DocumentBoxController extends AbstractController
{
	@Autowired private DocumentBoxServiceImpl documentBoxService;

	@Autowired private DocumentBoxSearchBean documentBoxSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = DocumentBox_.NAME) Pageable pageable)
	{
		Page<DocumentBoxDto> resultsPage = documentBoxService.find(documentBoxSearchBean.getDocumentBoxSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, documentBoxSearchBean.getDocumentBoxSearchDto());

		return ViewEnum.DOCUMENT_BOX_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) DocumentBoxSearchDto documentBoxSearchDto, Model model)
	{
		documentBoxSearchBean.setDocumentBoxSearchDto(documentBoxSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_BOX, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		DocumentBoxDto documentBox = documentBoxService.find(id);
		if (documentBox == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.DOCUMENT_BOX, documentBox);
		return ViewEnum.DOCUMENT_BOX_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.DOCUMENT_BOX, documentBoxService.create());
		return ViewEnum.DOCUMENT_BOX_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		DocumentBoxDto documentBox = documentBoxService.find(id);
		if (documentBox == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.DOCUMENT_BOX, documentBox);
		return ViewEnum.DOCUMENT_BOX_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		DocumentBoxDto documentBox = documentBoxService.find(id);
		if (documentBox == null)
			throw new NotFoundException(String.valueOf(id));

		try
		{
			if (documentBoxService.delete(id))
			{
				redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
						Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_BOX_DELETE_SUCCESS, documentBox.getName()));
			}
			else
			{
				redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
						Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_DELETE_ERROR, documentBox.getName()));
			}
		}
		catch (StillInUseException ex)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_DELETE_ERROR_STILL_IN_USE, ex.getCount()));
		}

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_BOX, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.DOCUMENT_BOX) @Valid DocumentBoxDto documentBox, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(documentBox, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_SAVE_INVALID));
			return ViewEnum.DOCUMENT_BOX_CREATE.getViewName();
		}

		if (documentBoxService.save(documentBox) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_BOX_SAVE_SUCCESS, documentBox.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_BOX, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_SAVE_ERROR));
		return ViewEnum.DOCUMENT_BOX_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.DOCUMENT_BOX) @Valid DocumentBoxDto documentBox, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(documentBox, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_UPDATE_INVALID));
			return ViewEnum.DOCUMENT_BOX_EDIT.getViewName();
		}

		if (documentBoxService.update(documentBox) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_BOX_UPDATE_SUCCESS, documentBox.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT_BOX, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_BOX_UPDATE_ERROR));
		return ViewEnum.DOCUMENT_BOX_EDIT.getViewName();
	}

	private boolean validate(DocumentBoxDto documentBox, BindingResult bindingResult)
	{
		ValidationResult validationResult = documentBoxService.validate(documentBox);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Document box %s does not exist.", ex.getMessage()));
	}
}
