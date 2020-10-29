package karstenroethig.paperless.webapp.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.Document_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.service.impl.ContactServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentBoxServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentTypeServiceImpl;

@ComponentScan
@Controller
public class DashboardController
{
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private ContactServiceImpl contactService;

	@GetMapping(value = {UrlMappings.HOME, UrlMappings.DASHBOARD})
	public String dashborad(Model model)
	{
		addAttributesForAdminCard(model);
		addAttributesForReviewCard(model);
		addAttributesForDeletionCard(model);

		return ViewEnum.DASHBOARD.getViewName();
	}

	private void addAttributesForAdminCard(Model model)
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
	}

	private void addAttributesForReviewCard(Model model)
	{
		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setReviewDateTo(LocalDate.now());

		Page<DocumentDto> documentReviewPage = documentService.find(documentSearch, PageRequest.of(0, 10, Sort.by(Document_.REVIEW_DATE)));
		model.addAttribute("documentReviewPage", documentReviewPage);
	}

	private void addAttributesForDeletionCard(Model model)
	{
		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setDeletionDateTo(LocalDate.now());

		Page<DocumentDto> documentDeletionPage = documentService.find(documentSearch, PageRequest.of(0, 10, Sort.by(Document_.DELETION_DATE)));
		model.addAttribute("documentDeletionPage", documentDeletionPage);
	}
}
