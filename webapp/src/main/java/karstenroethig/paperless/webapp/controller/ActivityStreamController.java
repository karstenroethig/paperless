package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.controller.util.ViewEnum;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeGroup_;
import karstenroethig.paperless.webapp.model.dto.changes.ActivityGroupDto;
import karstenroethig.paperless.webapp.service.impl.ActivityStreamServiceImpl;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_ACTIVITY_STREAM)
public class ActivityStreamController extends AbstractController
{
	@Autowired private ActivityStreamServiceImpl activityStreamService;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = ChangeGroup_.CREATED_DATETIME) Pageable pageable)
	{
		Page<ActivityGroupDto> resultsPage = activityStreamService.findAll(pageable);
		addPagingAttributes(model, resultsPage);

		return ViewEnum.ACTIVITY_STREAM_LIST.getViewName();
	}
}
