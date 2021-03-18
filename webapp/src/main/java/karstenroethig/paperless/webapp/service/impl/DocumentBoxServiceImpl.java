package karstenroethig.paperless.webapp.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.DocumentBox;
import karstenroethig.paperless.webapp.model.domain.DocumentBox_;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.repository.DocumentBoxRepository;
import karstenroethig.paperless.webapp.repository.specification.DocumentBoxSpecifications;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class DocumentBoxServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(DocumentBox_.NAME));

	private static final DocumentBoxSearchDto EMPTY_SEACH_PARAMS = new DocumentBoxSearchDto();

	@Autowired private DocumentServiceImpl documentService;

	@Autowired private DocumentBoxRepository documentBoxRepository;

	public DocumentBoxDto create()
	{
		return new DocumentBoxDto();
	}

	public ValidationResult validate(DocumentBoxDto documentBox)
	{
		ValidationResult result = new ValidationResult();

		if (documentBox == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(documentBox));

		return result;
	}

	private void checkValidation(DocumentBoxDto documentBox)
	{
		ValidationResult result = validate(documentBox);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(DocumentBoxDto documentBox)
	{
		ValidationResult result = new ValidationResult();

		DocumentBox existing = documentBoxRepository.findOneByNameIgnoreCase(documentBox.getName()).orElse(null);
		if (existing != null
				&& (documentBox.getId() == null
				|| !existing.getId().equals(documentBox.getId())))
			result.addError("name", MessageKeyEnum.DOCUMENT_BOX_SAVE_ERROR_EXISTS_NAME);

		return result;
	}

	public DocumentBoxDto save(DocumentBoxDto documentBoxDto)
	{
		checkValidation(documentBoxDto);

		DocumentBox documentBox = new DocumentBox();
		merge(documentBox, documentBoxDto);

		return transform(documentBoxRepository.save(documentBox));
	}

	public DocumentBoxDto update(DocumentBoxDto documentBoxDto)
	{
		checkValidation(documentBoxDto);

		DocumentBox documentBox = documentBoxRepository.findById(documentBoxDto.getId()).orElse(null);
		if (documentBox == null)
			return null;

		merge(documentBox, documentBoxDto);

		return transform(documentBoxRepository.save(documentBox));
	}

	public ValidationResult validateDelete(DocumentBoxDto documentBox)
	{
		ValidationResult result = new ValidationResult();

		if (documentBox == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setDocumentBox(documentBox);
		Page<DocumentDto> resultsPage = documentService.findBySearchParams(documentSearch, PageRequest.of(0, 1));
		if (resultsPage.hasContent())
			result.addError(MessageKeyEnum.DOCUMENT_BOX_DELETE_INVALID_STILL_IN_USE_BY_DOCUMENTS, resultsPage.getTotalElements());

		return result;
	}

	private void checkValidationDelete(DocumentBoxDto documentBox)
	{
		ValidationResult result = validateDelete(documentBox);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	public boolean delete(DocumentBoxDto documentBoxDto)
	{
		checkValidationDelete(documentBoxDto);

		DocumentBox documentBox = documentBoxRepository.findById(documentBoxDto.getId()).orElse(null);
		if (documentBox == null)
			return false;

		documentBoxRepository.delete(documentBox);
		return true;
	}

	public long count()
	{
		return documentBoxRepository.count();
	}

	public Page<DocumentBoxDto> findBySearchParams(DocumentBoxSearchDto documentBoxSearchDto, Pageable pageable)
	{
		Page<DocumentBox> page = documentBoxRepository.findAll(DocumentBoxSpecifications.matchesSearchParam(documentBoxSearchDto), pageable);
		return page.map(this::transform);
	}

	public List<DocumentBoxDto> findAllUnarchived()
	{
		Page<DocumentBoxDto> pageDto = findBySearchParams(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public Page<DocumentBoxDto> findAll(Pageable pageable)
	{
		Page<DocumentBox> page = documentBoxRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public DocumentBoxDto find(Long id)
	{
		return transform(documentBoxRepository.findById(id).orElse(null));
	}

	private DocumentBox merge(DocumentBox documentBox, DocumentBoxDto documentBoxDto)
	{
		if (documentBox == null || documentBoxDto == null )
			return null;

		documentBox.setName(documentBoxDto.getName());
		documentBox.setDescription(documentBoxDto.getDescription());
		documentBox.setArchived(documentBoxDto.isArchived());

		return documentBox;
	}

	protected DocumentBoxDto transform(DocumentBox documentBox)
	{
		if (documentBox == null)
			return null;

		DocumentBoxDto documentBoxDto = new DocumentBoxDto();

		documentBoxDto.setId(documentBox.getId());
		documentBoxDto.setName(documentBox.getName());
		documentBoxDto.setDescription(documentBox.getDescription());
		documentBoxDto.setArchived(documentBox.isArchived());

		return documentBoxDto;
	}

	protected DocumentBox transform(DocumentBoxDto documentBoxDto)
	{
		if (documentBoxDto == null || documentBoxDto.getId() == null)
			return null;

		return documentBoxRepository.findById(documentBoxDto.getId()).orElse(null);
	}
}
