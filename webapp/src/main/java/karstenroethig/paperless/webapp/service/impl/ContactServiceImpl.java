package karstenroethig.paperless.webapp.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Contact;
import karstenroethig.paperless.webapp.model.domain.Contact_;
import karstenroethig.paperless.webapp.model.dto.ContactDto;
import karstenroethig.paperless.webapp.model.dto.ContactSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto.ContactSearchTypeEnum;
import karstenroethig.paperless.webapp.repository.ContactRepository;
import karstenroethig.paperless.webapp.repository.specification.ContactSpecifications;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.service.exceptions.StillInUseException;

@Service
@Transactional
public class ContactServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Contact_.NAME));

	private static final ContactSearchDto EMPTY_SEACH_PARAMS = new ContactSearchDto();

	@Autowired private DocumentServiceImpl documentService;

	@Autowired private ContactRepository contactRepository;

	public ContactDto create()
	{
		return new ContactDto();
	}

	public ContactDto save(ContactDto contactDto) throws AlreadyExistsException
	{
		doesAlreadyExist(null, contactDto.getName());

		Contact contact = new Contact();
		merge(contact, contactDto);

		return transform(contactRepository.save(contact));
	}

	public ContactDto update(ContactDto contactDto) throws AlreadyExistsException
	{
		doesAlreadyExist(contactDto.getId(), contactDto.getName());

		Contact contact = contactRepository.findById(contactDto.getId()).orElse(null);
		merge(contact, contactDto);

		return transform(contactRepository.save(contact));
	}

	private void doesAlreadyExist(Long id, String name) throws AlreadyExistsException
	{
		Contact existing = contactRepository.findOneByNameIgnoreCase(name);
		if (existing != null
				&& (id == null
				|| !existing.getId().equals(id)))
			throw new AlreadyExistsException("name");
	}

	public boolean delete(Long id) throws StillInUseException
	{
		Contact contact = contactRepository.findById(id).orElse(null);
		if (contact == null)
			return false;

		DocumentSearchDto documentSearch = new DocumentSearchDto();
		documentSearch.setContact(transform(contact));
		documentSearch.setContactSearchType(ContactSearchTypeEnum.SENDER_OR_RECEIVER);
		Page<DocumentDto> resultsPage = documentService.find(documentSearch, PageRequest.of(0, 1));
		if (resultsPage.hasContent())
			throw new StillInUseException(resultsPage.getTotalElements());

		contactRepository.delete(contact);
		return true;
	}

	public Page<ContactDto> find(ContactSearchDto contactSearchDto, Pageable pageable)
	{
		Page<Contact> page = contactRepository.findAll(ContactSpecifications.matchesSearchParam(contactSearchDto), pageable);
		return page.map(this::transform);
	}

	public List<ContactDto> findAll()
	{
		Page<ContactDto> pageDto = find(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public ContactDto find(Long id)
	{
		return transform(contactRepository.findById(id).orElse(null));
	}

	private Contact merge(Contact contact, ContactDto contactDto)
	{
		if (contact == null || contactDto == null )
			return null;

		contact.setName(contactDto.getName());
		contact.setArchived(contactDto.isArchived());

		return contact;
	}

	protected ContactDto transform(Contact contact)
	{
		if (contact == null)
			return null;

		ContactDto contactDto = new ContactDto();

		contactDto.setId(contact.getId());
		contactDto.setName(contact.getName());
		contactDto.setArchived(contact.isArchived());

		return contactDto;
	}

	protected Contact transform(ContactDto contactDto)
	{
		if (contactDto == null || contactDto.getId() == null)
			return null;

		return contactRepository.findById(contactDto.getId()).orElse(null);
	}
}
