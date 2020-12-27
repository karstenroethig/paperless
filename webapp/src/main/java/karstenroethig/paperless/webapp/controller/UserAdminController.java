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
import org.springframework.security.access.annotation.Secured;
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

import karstenroethig.paperless.webapp.bean.UserSearchBean;
import karstenroethig.paperless.webapp.config.SecurityConfiguration.Authorities;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.User_;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto.NewRegisteredSearchTypeEnum;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.service.impl.UserAdminServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_USER_ADMIN)
@Secured(Authorities.ADMIN)
public class UserAdminController extends AbstractController
{
	@Autowired private UserAdminServiceImpl userService;

	@Autowired private UserSearchBean userSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = User_.USERNAME) Pageable pageable)
	{
		Page<UserDto> resultsPage = userService.find(userSearchBean.getUserSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, userSearchBean.getUserSearchDto());
		addBasicAttributes(model);

		return ViewEnum.USER_ADMIN_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.SEARCH_PARAMS) UserSearchDto userSearchDto, Model model)
	{
		userSearchBean.setUserSearchDto(userSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_USER_ADMIN, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SEARCH + "/new-registered")
	public String searchNewRegistered(Model model)
	{
		userSearchBean.clear();
		userSearchBean.getUserSearchDto().setNewRegisteredSearchType(NewRegisteredSearchTypeEnum.NEW_REGISTERED);

		return UrlMappings.redirect(UrlMappings.CONTROLLER_USER_ADMIN, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		UserDto user = userService.find(id);
		if (user == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.USER, user);

		return ViewEnum.USER_ADMIN_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.USER, userService.create());
		addBasicAttributes(model);
		return ViewEnum.USER_ADMIN_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		UserDto user = userService.find(id);
		if (user == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.USER, user);
		addBasicAttributes(model);
		return ViewEnum.USER_ADMIN_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		UserDto user = userService.find(id);
		if (user == null)
			throw new NotFoundException(String.valueOf(id));

		if (userService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.USER_ADMIN_DELETE_SUCCESS, user.getUsername()));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.USER_ADMIN_DELETE_ERROR, user.getUsername()));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_USER_ADMIN, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.USER) @Valid UserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(user, true, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_SAVE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_ADMIN_CREATE.getViewName();
		}

		try
		{
			if (userService.save(user) != null)
			{
				redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
						Messages.createWithSuccess(MessageKeyEnum.USER_ADMIN_SAVE_SUCCESS, user.getUsername()));
				return UrlMappings.redirect(UrlMappings.CONTROLLER_USER_ADMIN, UrlMappings.ACTION_LIST);
			}
		}
		catch (AlreadyExistsException ex)
		{
			bindingResult.rejectValue(ex.getFieldId(), MessageKeyEnum.USER_ADMIN_SAVE_ERROR_EXISTS.getKey());
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_SAVE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_ADMIN_CREATE.getViewName();
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_SAVE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.USER_ADMIN_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.USER) @Valid UserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(user, false, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_ADMIN_EDIT.getViewName();
		}

		try
		{
			if (userService.update(user) != null)
			{
				redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
						Messages.createWithSuccess(MessageKeyEnum.USER_ADMIN_UPDATE_SUCCESS, user.getUsername()));
				return UrlMappings.redirect(UrlMappings.CONTROLLER_USER_ADMIN, UrlMappings.ACTION_LIST);
			}
		}
		catch (AlreadyExistsException ex)
		{
			bindingResult.rejectValue(ex.getFieldId(), MessageKeyEnum.USER_ADMIN_SAVE_ERROR_EXISTS.getKey());
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_ADMIN_EDIT.getViewName();
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_ADMIN_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.USER_ADMIN_EDIT.getViewName();
	}

	private boolean validate(UserDto user, boolean forInitialCreation, BindingResult bindingResult)
	{
		ValidationResult validationResult = userService.validate(user, forInitialCreation);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	private void addBasicAttributes(Model model)
	{
		model.addAttribute("allAuthorities", Authorities.ALL_AUTHORITIES);
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("User %s does not exist.", ex.getMessage()));
	}
}
