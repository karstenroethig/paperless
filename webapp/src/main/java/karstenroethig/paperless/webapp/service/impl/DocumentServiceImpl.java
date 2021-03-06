package karstenroethig.paperless.webapp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.search.DocumentSearchDto;
import karstenroethig.paperless.webapp.repository.DocumentRepository;
import karstenroethig.paperless.webapp.repository.specification.DocumentSpecifications;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class DocumentServiceImpl
{
	@Autowired private ContactServiceImpl contactService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private TagServiceImpl tagService;

	@Autowired private DocumentRepository documentRepository;

	public DocumentDto create()
	{
		return new DocumentDto();
	}

	public ValidationResult validate(DocumentDto document)
	{
		ValidationResult result = new ValidationResult();

		if (document == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		return result;
	}

	private void checkValidation(DocumentDto document)
	{
		ValidationResult result = validate(document);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	public DocumentDto save(DocumentDto documentDto)
	{
		checkValidation(documentDto);

		Document document = new Document();
		document.setCreatedDatetime(LocalDateTime.now());
		merge(document, documentDto);

		return transform(documentRepository.save(document));
	}

	public DocumentDto update(DocumentDto documentDto)
	{
		checkValidation(documentDto);

		Document document = documentRepository.findById(documentDto.getId()).orElse(null);
		if (document == null)
			return null;

		merge(document, documentDto);

		return transform(documentRepository.save(document));
	}

	public boolean delete(Long id)
	{
		Document document = documentRepository.findById(id).orElse(null);
		if (document == null)
			return false;

		documentRepository.delete(document);
		return true;
	}

	protected void markUpdated(Long id)
	{
		Document document = documentRepository.findById(id).orElse(null);
		if (document == null)
			return;

		document.setUpdatedDatetime(LocalDateTime.now());
		documentRepository.save(document);
	}

	public long count()
	{
		return documentRepository.count();
	}

	public Page<DocumentDto> findBySearchParams(DocumentSearchDto documentSearchDto, Pageable pageable)
	{
		Page<Document> page = documentRepository.findAll(DocumentSpecifications.matchesSearchParam(documentSearchDto), pageable);
		return page.map(this::transform);
	}

	public Page<DocumentDto> findAll(Pageable pageable)
	{
		Page<Document> page = documentRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public DocumentDto find(Long id)
	{
		return transform(documentRepository.findById(id).orElse(null));
	}

	private Document merge(Document document, DocumentDto documentDto)
	{
		if (document == null || documentDto == null )
			return null;

		document.setTitle(documentDto.getTitle());
		document.setDocumentType(documentTypeService.transform(documentDto.getDocumentType()));
		document.setDateOfIssue(documentDto.getDateOfIssue());
		document.setSender(contactService.transform(documentDto.getSender()));
		document.setReceiver(contactService.transform(documentDto.getReceiver()));
		document.setDocumentBox(documentBoxService.transform(documentDto.getDocumentBox()));
		document.setDescription(documentDto.getDescription());
		document.setUpdatedDatetime(LocalDateTime.now());
		document.setReviewDate(documentDto.getReviewDate());
		document.setDeletionDate(documentDto.getDeletionDate());
		document.setArchived(documentDto.isArchived());

		mergeTags(document, documentDto);

		return document;
	}

	private Document mergeTags(Document document, DocumentDto documentDto)
	{
		// delete unassigned tags
		List<Tag> previousAssignedTags = new ArrayList<>(document.getTags());
		List<Long> newlyAssignedTagIds = documentDto.getTags().stream().map(TagDto::getId).collect(Collectors.toList());
		Set<Long> alreadyAssignedTagIds = new HashSet<>();

		for (Tag tag : previousAssignedTags)
		{
			if (newlyAssignedTagIds.contains(tag.getId()))
				alreadyAssignedTagIds.add(tag.getId());
			else
				document.removeTag(tag);
		}

		// add new assigned tags
		for (TagDto tag : documentDto.getTags())
		{
			if (!alreadyAssignedTagIds.contains(tag.getId()))
				document.addTag(tagService.transform(tag));
		}

		return document;
	}

	protected DocumentDto transform(Document document)
	{
		if (document == null)
			return null;

		DocumentDto documentDto = new DocumentDto();

		documentDto.setId(document.getId());
		documentDto.setTitle(document.getTitle());
		documentDto.setDocumentType(documentTypeService.transform(document.getDocumentType()));
		documentDto.setDateOfIssue(document.getDateOfIssue());
		documentDto.setSender(contactService.transform(document.getSender()));
		documentDto.setReceiver(contactService.transform(document.getReceiver()));
		documentDto.setDocumentBox(documentBoxService.transform(document.getDocumentBox()));
		documentDto.setDescription(document.getDescription());
		documentDto.setCreatedDatetime(document.getCreatedDatetime());
		documentDto.setUpdatedDatetime(document.getUpdatedDatetime());
		documentDto.setReviewDate(document.getReviewDate());
		documentDto.setDeletionDate(document.getDeletionDate());
		documentDto.setArchived(document.isArchived());

		Set<Tag> tags = document.getTags();
		if (tags != null && !tags.isEmpty())
			for (Tag tag : tags)
				documentDto.addTag(tagService.transform(tag));

		return documentDto;
	}

	protected Document transform(DocumentDto documentDto)
	{
		if (documentDto == null || documentDto.getId() == null)
			return null;

		return documentRepository.findById(documentDto.getId()).orElse(null);
	}
}
