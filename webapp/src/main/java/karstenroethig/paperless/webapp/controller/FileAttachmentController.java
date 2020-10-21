package karstenroethig.paperless.webapp.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.paperless.webapp.controller.exceptions.NotFoundException;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentDto;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentUploadDto;
import karstenroethig.paperless.webapp.service.impl.FileAttachmentServiceImpl;
import karstenroethig.paperless.webapp.service.impl.FileStorageServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_FILE_ATTACHMENT)
public class FileAttachmentController extends AbstractController
{
	@Autowired private FileAttachmentServiceImpl fileAttachmentService;
	@Autowired private FileStorageServiceImpl fileStorageService;

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	@ResponseBody
	public ResponseEntity<Resource> show(@PathVariable("id") Long id, @RequestParam(name = "inline", defaultValue = "true") boolean inline,
			Model model) throws IOException
	{
		FileAttachmentDto fileAttachment = fileAttachmentService.find(id);
		if (fileAttachment == null)
			throw new NotFoundException(String.valueOf(id));

		Resource fileResource = fileStorageService.loadAsResource(fileAttachment.getFileStorage());
		return ResponseEntity
				.ok()
				.contentType(MediaType.parseMediaType(fileAttachment.getContentType()))
				.contentLength(fileAttachment.getSize())
				.cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + fileAttachment.getName() + "\"")
				.body(fileResource);
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model) throws IOException
	{
		FileAttachmentDto fileAttachment = fileAttachmentService.find(id);
		if (fileAttachment == null)
			throw new NotFoundException(String.valueOf(id));

		if (fileAttachmentService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.FILE_ATTACHMENT_DELETE_SUCCESS, fileAttachment.getName()));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.FILE_ATTACHMENT_DELETE_ERROR, fileAttachment.getName()));

		return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, fileAttachment.getDocumentId());
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute("fileAttachmentUpload") @Valid FileAttachmentUploadDto fileAttachmentUpload, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model) throws IOException
	{
		if (bindingResult.hasErrors())
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.FILE_ATTACHMENT_SAVE_INVALID));
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, fileAttachmentUpload.getDocument().getId());
		}

		List<FileAttachmentDto> savedFiles = fileAttachmentService.save(fileAttachmentUpload);
		if (!savedFiles.isEmpty())
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.FILE_ATTACHMENT_SAVE_SUCCESS, savedFiles.size()));
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, fileAttachmentUpload.getDocument().getId());
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.FILE_ATTACHMENT_SAVE_ERROR));
		return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_DOCUMENT, UrlMappings.ACTION_SHOW, fileAttachmentUpload.getDocument().getId());
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("File %s does not exist.", ex.getMessage()));
	}
}
