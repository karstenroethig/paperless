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
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.repository.DocumentBoxRepository;
import karstenroethig.paperless.webapp.repository.specification.DocumentBoxSpecifications;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.service.exceptions.StillInUseException;

@Service
@Transactional
public class DocumentBoxServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("name"));

	private static final DocumentBoxSearchDto EMPTY_SEACH_PARAMS = new DocumentBoxSearchDto();

	@Autowired private DocumentServiceImpl documentService;

	@Autowired private DocumentBoxRepository documentBoxRepository;

	public DocumentBoxDto create()
	{
		return new DocumentBoxDto();
	}

	public DocumentBoxDto save(DocumentBoxDto documentBoxDto) throws AlreadyExistsException
	{
		doesAlreadyExist(null, documentBoxDto.getName());

		DocumentBox documentBox = new DocumentBox();
		merge(documentBox, documentBoxDto);

		return transform(documentBoxRepository.save(documentBox));
	}

	public DocumentBoxDto update(DocumentBoxDto documentBoxDto) throws AlreadyExistsException
	{
		doesAlreadyExist(documentBoxDto.getId(), documentBoxDto.getName());

		DocumentBox documentBox = documentBoxRepository.findById(documentBoxDto.getId()).orElse(null);
		merge(documentBox, documentBoxDto);

		return transform(documentBoxRepository.save(documentBox));
	}

	private void doesAlreadyExist(Long id, String name) throws AlreadyExistsException
	{
		DocumentBox existing = documentBoxRepository.findOneByNameIgnoreCase(name);
		if (existing != null
				&& (id == null
				|| !existing.getId().equals(id)))
			throw new AlreadyExistsException("name");
	}

	public boolean delete(Long id) throws StillInUseException
	{
		DocumentBox documentBox = documentBoxRepository.findById(id).orElse(null);
		if (documentBox == null)
			return false;

		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setDocumentBox(transform(documentBox));
		Page<DocumentDto> resultsPage = documentService.find(documentSearch, PageRequest.of(0, 1));
		if (resultsPage.hasContent())
			throw new StillInUseException(resultsPage.getTotalElements());

		documentBoxRepository.delete(documentBox);
		return true;
	}

	public Page<DocumentBoxDto> find(DocumentBoxSearchDto documentBoxSearchDto, Pageable pageable)
	{
		Page<DocumentBox> page = documentBoxRepository.findAll(DocumentBoxSpecifications.matchesSearchParam(documentBoxSearchDto), pageable);
		Page<DocumentBoxDto> pageDto = page.map(this::transform);

		return pageDto;
	}

	public List<DocumentBoxDto> findAll()
	{
		Page<DocumentBoxDto> pageDto = find(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
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
