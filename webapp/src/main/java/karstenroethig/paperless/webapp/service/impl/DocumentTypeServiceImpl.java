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
import karstenroethig.paperless.webapp.service.exceptions.StillInUseException;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

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

	public ValidationResult validate(DocumentTypeDto documentType)
	{
		ValidationResult result = new ValidationResult();

		if (documentType == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(documentType));

		return result;
	}

	private void checkValidation(DocumentTypeDto documentType)
	{
		ValidationResult result = validate(documentType);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(DocumentTypeDto documentType)
	{
		ValidationResult result = new ValidationResult();

		DocumentType existing = documentTypeRepository.findOneByNameIgnoreCase(documentType.getName()).orElse(null);
		if (existing != null
				&& (documentType.getId() == null
				|| !existing.getId().equals(documentType.getId())))
			result.addError(MessageKeyEnum.DOCUMENT_TYPE_SAVE_ERROR_EXISTS, "name");

		return result;
	}

	public DocumentTypeDto save(DocumentTypeDto documentTypeDto)
	{
		checkValidation(documentTypeDto);

		DocumentType documentType = new DocumentType();
		merge(documentType, documentTypeDto);

		return transform(documentTypeRepository.save(documentType));
	}

	public DocumentTypeDto update(DocumentTypeDto documentTypeDto)
	{
		checkValidation(documentTypeDto);

		DocumentType documentType = documentTypeRepository.findById(documentTypeDto.getId()).orElse(null);
		if (documentType == null)
			return null;

		merge(documentType, documentTypeDto);

		return transform(documentTypeRepository.save(documentType));
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
		return page.map(this::transform);
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
