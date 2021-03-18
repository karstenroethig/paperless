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

import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.Group_;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.service.impl.GroupServiceImpl;
import karstenroethig.paperless.webapp.service.impl.UserAdminServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_GROUP)
public class GroupController extends AbstractController
{
	@Autowired private GroupServiceImpl groupService;
	@Autowired private UserAdminServiceImpl userService;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = Group_.NAME) Pageable pageable)
	{
		Page<GroupDto> resultsPage = groupService.findAll(pageable);
		addPagingAttributes(model, resultsPage);

		return ViewEnum.GROUP_LIST.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		GroupDto group = groupService.find(id);
		if (group == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.GROUP, group);
		return ViewEnum.GROUP_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.GROUP, groupService.create());
		addBasicAttributes(model);
		return ViewEnum.GROUP_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		GroupDto group = groupService.find(id);
		if (group == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.GROUP, group);
		addBasicAttributes(model);
		return ViewEnum.GROUP_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		GroupDto group = groupService.find(id);
		if (group == null)
			throw new NotFoundException(String.valueOf(id));

		if (groupService.delete(id))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.GROUP_DELETE_SUCCESS, group.getName()));
		}
		else
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.GROUP_DELETE_ERROR, group.getName()));
		}

		return UrlMappings.redirect(UrlMappings.CONTROLLER_GROUP, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.GROUP) @Valid GroupDto group, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(group, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.GROUP_SAVE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.GROUP_CREATE.getViewName();
		}

		if (groupService.save(group) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.GROUP_SAVE_SUCCESS, group.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_GROUP, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.GROUP_SAVE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.GROUP_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.GROUP) @Valid GroupDto group, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(group, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.GROUP_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.GROUP_EDIT.getViewName();
		}

		if (groupService.update(group) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.GROUP_UPDATE_SUCCESS, group.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_GROUP, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.GROUP_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.GROUP_EDIT.getViewName();
	}

	private boolean validate(GroupDto group, BindingResult bindingResult)
	{
		ValidationResult validationResult = groupService.validate(group);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	private void addBasicAttributes(Model model)
	{
		model.addAttribute("allUsers", userService.findAll());
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Group %s does not exist.", ex.getMessage()));
	}
}
