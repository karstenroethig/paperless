package karstenroethig.paperless.webapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.FileAttachment;
import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.repository.DocumentRepository;
import karstenroethig.paperless.webapp.repository.specification.DocumentSpecifications;

@Service
@Transactional
public class DocumentServiceImpl
{
	@Autowired private ContactServiceImpl contactService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private FileAttachmentServiceImpl fileAttachmentService;
	@Autowired private TagServiceImpl tagService;

	@Autowired private DocumentRepository documentRepository;

	public DocumentDto create()
	{
		return new DocumentDto();
	}

	public DocumentDto save(DocumentDto documentDto)
	{
		Document document = new Document();
		document.setCreatedDatetime(LocalDateTime.now());
		merge(document, documentDto);

		return transform(documentRepository.save(document));
	}

	public DocumentDto update(DocumentDto documentDto)
	{
		Document document = documentRepository.findById(documentDto.getId()).orElse(null);
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

	public Page<DocumentDto> find(DocumentSearchDto documentSearchDto, Pageable pageable)
	{
		Page<Document> page = documentRepository.findAll(DocumentSpecifications.matchesSearchParam(documentSearchDto), pageable);
		Page<DocumentDto> pageDto = page.map(this::transform);

		return pageDto;
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

		document.clearTags();
		for (TagDto tagDto : documentDto.getTags())
			document.addTag(tagService.transform(tagDto));

		return document;
	}

	private DocumentDto transform(Document document)
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

		List<FileAttachment> fileAttachments = document.getFileAttachments();
		if (fileAttachments != null && !fileAttachments.isEmpty())
			for (FileAttachment fileAttachment : fileAttachments)
				documentDto.addFileAttachment(fileAttachmentService.transform(fileAttachment));

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
