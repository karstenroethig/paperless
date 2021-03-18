package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.paperless.webapp.config.ApplicationProperties;
import karstenroethig.paperless.webapp.config.SecurityConfiguration.Authorities;
import karstenroethig.paperless.webapp.controller.util.AttributeNames;
import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.service.impl.BackupInitializerServiceImpl;
import karstenroethig.paperless.webapp.service.impl.BackupServiceImpl;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.Messages;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_BACKUP)
@Secured(Authorities.ADMIN)
public class BackupController
{
	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private BackupServiceImpl backupService;
	@Autowired private BackupInitializerServiceImpl backupInitializerService;

	@GetMapping()
	public String backup(Model model)
	{
		model.addAttribute("backupSettings", applicationProperties.getBackup());
		model.addAttribute("backupInfo", backupService.getBackupInfo());

		return ViewEnum.BACKUP_INDEX.getViewName();
	}

	@GetMapping(value = "/now")
	public String backupNow(Model model, final RedirectAttributes redirectAttributes)
	{
		if (backupInitializerService.backupNow())
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithSuccess(MessageKeyEnum.BACKUP_NOW_SUCCESS));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithWarning(MessageKeyEnum.BACKUP_NOW_ERROR));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_BACKUP);
	}

	@GetMapping(value = "/restore")
	public String restore(Model model)
	{
//		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithInfo(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY));

		return ViewEnum.BACKUP_RESTORE.getViewName();
	}
}
