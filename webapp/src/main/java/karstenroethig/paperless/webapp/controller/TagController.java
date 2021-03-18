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

import karstenroethig.paperless.webapp.bean.TagSearchBean;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.TagSearchDto;
import karstenroethig.paperless.webapp.service.impl.TagServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_TAG)
public class TagController extends AbstractController
{
	@Autowired private TagServiceImpl tagService;

	@Autowired private TagSearchBean tagSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = "name") Pageable pageable)
	{
		Page<TagDto> resultsPage = tagService.findBySearchParams(tagSearchBean.getTagSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, tagSearchBean.getTagSearchDto());

		return ViewEnum.TAG_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) TagSearchDto tagSearchDto, Model model)
	{
		tagSearchBean.setTagSearchDto(tagSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_TAG, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		TagDto tag = tagService.find(id);
		if (tag == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.TAG, tag);
		return ViewEnum.TAG_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.TAG, tagService.create());
		return ViewEnum.TAG_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		TagDto tag = tagService.find(id);
		if (tag == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.TAG, tag);
		return ViewEnum.TAG_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		TagDto tag = tagService.find(id);
		if (tag == null)
			throw new NotFoundException(String.valueOf(id));

		if (tagService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.TAG_DELETE_SUCCESS, tag.getName()));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.TAG_DELETE_ERROR, tag.getName()));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_TAG, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.TAG) @Valid TagDto tag, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(tag, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.TAG_SAVE_INVALID));
			return ViewEnum.TAG_CREATE.getViewName();
		}

		if (tagService.save(tag) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.TAG_SAVE_SUCCESS, tag.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_TAG, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.TAG_SAVE_ERROR));
		return ViewEnum.TAG_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.TAG) @Valid TagDto tag, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(tag, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.TAG_UPDATE_INVALID));
			return ViewEnum.TAG_EDIT.getViewName();
		}

		if (tagService.update(tag) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.TAG_UPDATE_SUCCESS, tag.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_TAG, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.TAG_UPDATE_ERROR));
		return ViewEnum.TAG_EDIT.getViewName();
	}

	private boolean validate(TagDto tag, BindingResult bindingResult)
	{
		ValidationResult validationResult = tagService.validate(tag);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Tag %s does not exist.", ex.getMessage()));
	}
}
