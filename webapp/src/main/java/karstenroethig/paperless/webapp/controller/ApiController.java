package karstenroethig.paperless.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import karstenroethig.paperless.webapp.controller.util.UrlMappings;
import karstenroethig.paperless.webapp.model.dto.api.TagUsageApiDto;
import karstenroethig.paperless.webapp.service.impl.TagServiceImpl;

@RestController
@RequestMapping(UrlMappings.CONTROLLER_API + UrlMappings.CONTROLLER_API_VERSION_1_0)
public class ApiController
{
	@Autowired private TagServiceImpl tagService;

	@GetMapping(value = "/tag/{id}/usage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagUsageApiDto> fetchTagUsage(@PathVariable("id") Long tagId)
	{
		TagUsageApiDto tagUsage = tagService.findUsage(tagId);

		return new ResponseEntity<>(tagUsage, HttpStatus.OK);
	}
}
