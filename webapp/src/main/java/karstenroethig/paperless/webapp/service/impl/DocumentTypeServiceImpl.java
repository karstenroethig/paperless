package karstenroethig.paperless.webapp.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.DocumentType;
import karstenroethig.paperless.webapp.model.domain.DocumentType_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeSearchDto;
import karstenroethig.paperless.webapp.repository.DocumentTypeRepository;
import karstenroethig.paperless.webapp.repository.specification.DocumentTypeSpecifications;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.service.exceptions.StillInUseException;

@Service
@Transactional
public class DocumentTypeServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(DocumentType_.NAME));

	private static final DocumentTypeSearchDto EMPTY_SEACH_PARAMS = new DocumentTypeSearchDto();

	@Autowired private DocumentServiceImpl documentService;

	@Autowired private DocumentTypeRepository documentTypeRepository;

	public DocumentTypeDto create()
	{
		return new DocumentTypeDto();
	}

	public DocumentTypeDto save(DocumentTypeDto documentTypeDto) throws AlreadyExistsException
	{
		doesAlreadyExist(null, documentTypeDto.getName());

		DocumentType documentType = new DocumentType();
		merge(documentType, documentTypeDto);

		return transform(documentTypeRepository.save(documentType));
	}

	public DocumentTypeDto update(DocumentTypeDto documentTypeDto) throws AlreadyExistsException
	{
		doesAlreadyExist(documentTypeDto.getId(), documentTypeDto.getName());

		DocumentType documentType = documentTypeRepository.findById(documentTypeDto.getId()).orElse(null);
		merge(documentType, documentTypeDto);

		return transform(documentTypeRepository.save(documentType));
	}

	private void doesAlreadyExist(Long id, String name) throws AlreadyExistsException
	{
		DocumentType existing = documentTypeRepository.findOneByNameIgnoreCase(name);
		if (existing != null
				&& (id == null
				|| !existing.getId().equals(id)))
			throw new AlreadyExistsException("name");
	}

	public boolean delete(Long id) throws StillInUseException
	{
		DocumentType documentType = documentTypeRepository.findById(id).orElse(null);
		if (documentType == null)
			return false;

		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setDocumentType(transform(documentType));
		Page<DocumentDto> resultsPage = documentService.find(documentSearch, PageRequest.of(0, 1));
		if (resultsPage.hasContent())
			throw new StillInUseException(resultsPage.getTotalElements());

		documentTypeRepository.delete(documentType);
		return true;
	}

	public Page<DocumentTypeDto> find(DocumentTypeSearchDto documentTypeSearchDto, Pageable pageable)
	{
		Page<DocumentType> page = documentTypeRepository.findAll(DocumentTypeSpecifications.matchesSearchParam(documentTypeSearchDto), pageable);
		Page<DocumentTypeDto> pageDto = page.map(this::transform);

		return pageDto;
	}

	public List<DocumentTypeDto> findAll()
	{
		Page<DocumentTypeDto> pageDto = find(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public DocumentTypeDto find(Long id)
	{
		return transform(documentTypeRepository.findById(id).orElse(null));
	}

	private DocumentType merge(DocumentType documentType, DocumentTypeDto documentTypeDto)
	{
		if (documentType == null || documentTypeDto == null )
			return null;

		documentType.setName(documentTypeDto.getName());
		documentType.setDescription(documentTypeDto.getDescription());
		documentType.setArchived(documentTypeDto.isArchived());

		return documentType;
	}

	protected DocumentTypeDto transform(DocumentType documentType)
	{
		if (documentType == null)
			return null;

		DocumentTypeDto documentTypeDto = new DocumentTypeDto();

		documentTypeDto.setId(documentType.getId());
		documentTypeDto.setName(documentType.getName());
		documentTypeDto.setDescription(documentType.getDescription());
		documentTypeDto.setArchived(documentType.isArchived());

		return documentTypeDto;
	}

	protected DocumentType transform(DocumentTypeDto documentTypeDto)
	{
		if (documentTypeDto == null || documentTypeDto.getId() == null)
			return null;

		return documentTypeRepository.findById(documentTypeDto.getId()).orElse(null);
	}
}
