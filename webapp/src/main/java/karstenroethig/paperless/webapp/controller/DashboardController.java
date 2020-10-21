package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.service.impl.ContactServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentBoxServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentTypeServiceImpl;

@ComponentScan
@Controller
public class DashboardController
{
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private ContactServiceImpl contactService;

	@GetMapping(value = {UrlMappings.HOME, UrlMappings.DASHBOARD})
	public String dashborad(Model model)
	{
		int numberOfNewRegisteredUsers = 5;
		boolean hasNoDocumentTypes = documentTypeService.findAll().isEmpty();
		boolean hasNoDocumentBoxes = documentBoxService.findAll().isEmpty();
		boolean hasNoContacts = contactService.findAll().isEmpty();

		boolean showAdminCard = numberOfNewRegisteredUsers > 0
				|| hasNoDocumentTypes
				|| hasNoDocumentBoxes
				|| hasNoContacts;

		model.addAttribute("numberOfNewRegisteredUsers", numberOfNewRegisteredUsers);
		model.addAttribute("hasNoDocumentTypes", hasNoDocumentTypes);
		model.addAttribute("hasNoDocumentBoxes", hasNoDocumentBoxes);
		model.addAttribute("hasNoContacts", hasNoContacts);
		model.addAttribute("showAdminCard", showAdminCard);

		return ViewEnum.DASHBOARD.getViewName();
	}
}
