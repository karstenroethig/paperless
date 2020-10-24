package karstenroethig.paperless.webapp.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.paperless.webapp.bean.DocumentSearchBean;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.service.impl.ContactServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentBoxServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentTypeServiceImpl;
import karstenroethig.paperless.webapp.service.impl.FileAttachmentServiceImpl;
import karstenroethig.paperless.webapp.service.impl.TagServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_DOCUMENT)
public class DocumentController extends AbstractController
{
	@Autowired private ContactServiceImpl contactService;
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private FileAttachmentServiceImpl fileAttachmentService;
	@Autowired private TagServiceImpl tagService;

	@Autowired private DocumentSearchBean documentSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = "title") Pageable pageable)
	{
		Page<DocumentDto> resultsPage = documentService.find(documentSearchBean.getDocumentSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, documentSearchBean.getDocumentSearchDto());
		addBasicAttributes(model);

		return ViewEnum.DOCUMENT_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) DocumentSearchDto documentSearchDto, Model model)
	{
		if (documentSearchDto.getDocumentType() != null)
			documentSearchDto.setDocumentType(documentTypeService.find(documentSearchDto.getDocumentType().getId()));
		if (documentSearchDto.getContact() != null)
			documentSearchDto.setContact(contactService.find(documentSearchDto.getContact().getId()));
		if (documentSearchDto.getTags() != null)
			documentSearchDto.setTags(documentSearchDto.getTags().stream().map(tag -> tagService.find(tag.getId())).collect(Collectors.toList()));
		if (documentSearchDto.getDocumentBox() != null)
			documentSearchDto.setDocumentBox(documentBoxService.find(documentSearchDto.getDocumentBox().getId()));

		documentSearchBean.setDocumentSearchDto(documentSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@RequestParam Long tagId, Model model)
	{
		TagDto tag = tagService.find(tagId);

		documentSearchBean.clear();
		documentSearchBean.getDocumentSearchDto().setTags(Arrays.asList(tag));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SEARCH + "/review")
	public String searchReview(Model model)
	{
		documentSearchBean.clear();
		documentSearchBean.getDocumentSearchDto().setReviewDateTo(LocalDate.now());

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SEARCH + "/deletion")
	public String searchDeletion(Model model)
	{
		documentSearchBean.clear();
		documentSearchBean.getDocumentSearchDto().setDeletionDateTo(LocalDate.now());

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		DocumentDto document = documentService.find(id);
		if (document == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute("document", document);
		model.addAttribute("fileAttachmentUpload", fileAttachmentService.create(document));
		return ViewEnum.DOCUMENT_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute("document", documentService.create());
		addBasicAttributes(model);
		return ViewEnum.DOCUMENT_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		DocumentDto document = documentService.find(id);
		if (document == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute("document", document);
		addBasicAttributes(model);
		return ViewEnum.DOCUMENT_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		DocumentDto document = documentService.find(id);
		if (document == null)
			throw new NotFoundException(String.valueOf(id));

		if (documentService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_DELETE_SUCCESS, document.getTitle()));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.DOCUMENT_DELETE_ERROR, document.getTitle()));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute("document") @Valid DocumentDto document, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (bindingResult.hasErrors())
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_SAVE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.DOCUMENT_CREATE.getViewName();
		}

		if (documentService.save(document) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_SAVE_SUCCESS, document.getTitle()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_SAVE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.DOCUMENT_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute("document") @Valid DocumentDto document, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (bindingResult.hasErrors())
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.DOCUMENT_EDIT.getViewName();
		}

		if (documentService.update(document) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.DOCUMENT_UPDATE_SUCCESS, document.getTitle()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.DOCUMENT_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.DOCUMENT_EDIT.getViewName();
	}

	private void addBasicAttributes(Model model)
	{
		model.addAttribute("allContacts", contactService.findAll());
		model.addAttribute("allDocumentBoxes", documentBoxService.findAll());
		model.addAttribute("allDocumentTypes", documentTypeService.findAll());
		model.addAttribute("allTags", tagService.findAll());
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Document %s does not exist.", ex.getMessage()));
	}
}
