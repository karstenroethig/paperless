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
import karstenroethig.paperless.webapp.model.domain.User_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.model.dto.search.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.search.UserSearchDto;
import karstenroethig.paperless.webapp.model.dto.search.UserSearchDto.NewRegisteredSearchTypeEnum;
import karstenroethig.paperless.webapp.service.impl.ContactServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentBoxServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentServiceImpl;
import karstenroethig.paperless.webapp.service.impl.DocumentTypeServiceImpl;
import karstenroethig.paperless.webapp.service.impl.UserAdminServiceImpl;

@ComponentScan
@Controller
public class DashboardController
{
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private ContactServiceImpl contactService;
	@Autowired private UserAdminServiceImpl userService;

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
		UserSearchDto userSearch = new UserSearchDto();
		userSearch.setNewRegisteredSearchType(NewRegisteredSearchTypeEnum.NEW_REGISTERED);

		Page<UserDto> newRegisteredUserPage = userService.findBySearchParams(userSearch, PageRequest.of(0, 1, Sort.by(User_.USERNAME)));
		long numberOfNewRegisteredUsers = newRegisteredUserPage.getTotalElements();

		boolean hasNoDocumentTypes = documentTypeService.findAllUnarchived().isEmpty();
		boolean hasNoDocumentBoxes = documentBoxService.findAllUnarchived().isEmpty();
		boolean hasNoContacts = contactService.findAllUnarchived().isEmpty();

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

		Page<DocumentDto> documentReviewPage = documentService.findBySearchParams(documentSearch, PageRequest.of(0, 10, Sort.by(Document_.REVIEW_DATE)));
		model.addAttribute("documentReviewPage", documentReviewPage);
	}

	private void addAttributesForDeletionCard(Model model)
	{
		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setDeletionDateTo(LocalDate.now());

		Page<DocumentDto> documentDeletionPage = documentService.findBySearchParams(documentSearch, PageRequest.of(0, 10, Sort.by(Document_.DELETION_DATE)));
		model.addAttribute("documentDeletionPage", documentDeletionPage);
	}
}
