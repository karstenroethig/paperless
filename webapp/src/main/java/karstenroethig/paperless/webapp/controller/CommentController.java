package karstenroethig.paperless.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
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
import karstenroethig.paperless.webapp.model.dto.CommentDto;
import karstenroethig.paperless.webapp.service.impl.CommentServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_COMMENT)
public class CommentController extends AbstractController
{
	@Autowired private CommentServiceImpl commentService;

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		CommentDto comment = commentService.find(id);
		if (comment == null)
			throw new NotFoundException(String.valueOf(id));

		if (commentService.delete(id))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.COMMENT_DELETE_SUCCESS));
		}
		else
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.COMMENT_DELETE_ERROR));
		}

		return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, comment.getDocument().getId());
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute("newComment") @Valid CommentDto comment, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (bindingResult.hasErrors())
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.COMMENT_SAVE_INVALID));
			return ViewEnum.DOCUMENT_SHOW.getViewName();
		}

		if (commentService.save(comment) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.COMMENT_SAVE_SUCCESS));
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, comment.getDocument().getId());
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.COMMENT_SAVE_ERROR));
		return ViewEnum.DOCUMENT_SHOW.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute("editComment") @Valid CommentDto comment, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (bindingResult.hasErrors())
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.COMMENT_UPDATE_INVALID));
			return ViewEnum.DOCUMENT_SHOW.getViewName();
		}

		if (commentService.update(comment) != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.COMMENT_UPDATE_SUCCESS));
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, comment.getDocument().getId());
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.COMMENT_UPDATE_ERROR));
		return ViewEnum.DOCUMENT_SHOW.getViewName();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Comment %s does not exist.", ex.getMessage()));
	}
}
