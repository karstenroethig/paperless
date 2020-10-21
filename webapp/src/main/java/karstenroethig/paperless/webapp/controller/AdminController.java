package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.dto.info.ServerInfoDto;
import karstenroethig.paperless.webapp.service.impl.ServerInfoServiceImpl;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_ADMIN)
public class AdminController
{
	@Autowired ServerInfoServiceImpl serverInfoService;

	@GetMapping(value = "/server-info")
	public String serverInfo(Model model)
	{
		ServerInfoDto serverInfo = serverInfoService.getInfo();

		model.addAttribute("serverInfo", serverInfo);

		return ViewEnum.ADMIN_SERVER_INFO.getViewName();
	}
}
