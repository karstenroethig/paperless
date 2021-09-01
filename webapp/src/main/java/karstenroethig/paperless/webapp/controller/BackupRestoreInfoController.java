package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.service.impl.RestoreServiceImpl;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_BACKUP)
public class BackupRestoreInfoController extends AbstractController
{
	@Autowired private RestoreServiceImpl restoreService;

	@GetMapping(value = "/restore-status")
	public String restoreStatus(Model model)
	{
		model.addAttribute("restoreInfo", restoreService.getRestoreInfo());

		return ViewEnum.BACKUP_RESTORE_STATUS.getViewName();
	}
}
