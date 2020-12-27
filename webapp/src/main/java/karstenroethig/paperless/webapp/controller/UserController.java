package karstenroethig.paperless.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.paperless.webapp.controller.exceptions.ForbiddenException;
import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.service.impl.UserServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping
public class UserController extends AbstractController
{
	@Autowired private UserServiceImpl userService;

	@GetMapping(value = "/login")
	public String login(Model model, HttpServletRequest request)
	{
		Exception exception = (Exception)request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
		if (exception != null)
			model.addAttribute("authMsg", exception.getMessage());

		return ViewEnum.USER_LOGIN.getViewName();
	}

	@GetMapping(value = "/access-denied")
	public String accessDenied(Model model) throws ForbiddenException
	{
		throw new ForbiddenException();
	}

	@GetMapping(value = "/logout-success")
	public String logoutSuccess(Model model)
	{
		return ViewEnum.USER_LOGOUT_SUCCESS.getViewName();
	}

	@GetMapping(value = "/register")
	public String register(Model model)
	{
		model.addAttribute(AttributeNames.USER, userService.create());
		addBasicAttributes(model);
		return ViewEnum.USER_REGISTER.getViewName();
	}

	@PostMapping(value = "/register")
	public String register(@ModelAttribute(AttributeNames.USER) @Valid UserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(user, true, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_SAVE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_REGISTER.getViewName();
		}

		try
		{
			if (userService.save(user) != null)
				return ViewEnum.USER_REGISTER_SUCCESS.getViewName();
		}
		catch (AlreadyExistsException ex)
		{
			bindingResult.rejectValue(ex.getFieldId(), MessageKeyEnum.USER_SAVE_ERROR_EXISTS.getKey());
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_SAVE_INVALID));
			return ViewEnum.USER_REGISTER.getViewName();
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_SAVE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.USER_REGISTER.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/show")
	public String show(Model model)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
			throw new NotFoundException("user not found");

		String username = authentication.getName();
		UserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			user.addSessionAuthority(authority.getAuthority());

		model.addAttribute(AttributeNames.USER, user);

		return ViewEnum.USER_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/edit")
	public String edit(Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		model.addAttribute(AttributeNames.USER, user);
		addBasicAttributes(model);
		return ViewEnum.USER_EDIT.getViewName();
	}

	@PostMapping(value = UrlMappings.CONTROLLER_USER + "/update")
	public String update(@ModelAttribute(AttributeNames.USER) @Valid UserDto user, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserDto currentUser = userService.find(username);
		if (currentUser == null)
			throw new NotFoundException(username);
		user.setId(currentUser.getId());

		if (!validate(user, false, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_EDIT.getViewName();
		}

		try
		{
			if (userService.update(user) != null)
			{
				redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
						Messages.createWithSuccess(MessageKeyEnum.USER_UPDATE_SUCCESS));
				return UrlMappings.redirect(UrlMappings.CONTROLLER_USER, "/show");
			}
		}
		catch (AlreadyExistsException ex)
		{
			bindingResult.rejectValue(ex.getFieldId(), MessageKeyEnum.USER_SAVE_ERROR_EXISTS.getKey());
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.USER_EDIT.getViewName();
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.USER_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.USER_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.CONTROLLER_USER + "/delete")
	public String delete(final RedirectAttributes redirectAttributes, Model model)
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserDto user = userService.find(username);
		if (user == null)
			throw new NotFoundException(username);

		if (userService.delete(user.getId()))
			return UrlMappings.redirect(UrlMappings.LOGOUT);

		redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
			Messages.createWithError(MessageKeyEnum.USER_DELETE_ERROR));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_USER, "/show");
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
//		model.addAttribute("allContacts", contactService.findAll());
//		model.addAttribute("allDocumentBoxes", documentBoxService.findAll());
//		model.addAttribute("allDocumentTypes", documentTypeService.findAll());
//		model.addAttribute("allTags", tagService.findAll());
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("User %s does not exist.", ex.getMessage()));
	}
}
