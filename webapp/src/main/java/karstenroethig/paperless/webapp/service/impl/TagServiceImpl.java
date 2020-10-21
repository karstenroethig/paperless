package karstenroethig.paperless.webapp.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.TagSearchDto;
import karstenroethig.paperless.webapp.model.dto.api.TagUsageApiDto;
import karstenroethig.paperless.webapp.repository.TagRepository;
import karstenroethig.paperless.webapp.repository.specification.TagSpecifications;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;

@Service
@Transactional
public class TagServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("name"));

	private static final TagSearchDto EMPTY_SEACH_PARAMS = new TagSearchDto();

	@Autowired private TagRepository tagRepository;

	public TagDto create()
	{
		return new TagDto();
	}

	public TagDto save(TagDto tagDto) throws AlreadyExistsException
	{
		doesAlreadyExist(null, tagDto.getName());

		Tag tag = new Tag();
		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	public TagDto update(TagDto tagDto) throws AlreadyExistsException
	{
		doesAlreadyExist(tagDto.getId(), tagDto.getName());

		Tag tag = tagRepository.findById(tagDto.getId()).orElse(null);
		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	private void doesAlreadyExist(Long id, String name) throws AlreadyExistsException
	{
		Tag existing = tagRepository.findOneByNameIgnoreCase(name);
		if (existing != null
				&& (id == null
				|| !existing.getId().equals(id)))
			throw new AlreadyExistsException("name");
	}

	public boolean delete(Long id)
	{
		Tag tag = tagRepository.findById(id).orElse(null);
		if (tag == null)
			return false;

		tagRepository.delete(tag);
		return true;
	}

	public Page<TagDto> find(TagSearchDto tagSearchDto, Pageable pageable)
	{
		Page<Tag> page = tagRepository.findAll(TagSpecifications.matchesSearchParam(tagSearchDto), pageable);
		Page<TagDto> pageDto = page.map(this::transform);

		return pageDto;
	}

	public List<TagDto> findAll()
	{
		Page<TagDto> pageDto = find(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public TagDto find(Long id)
	{
		return transform(tagRepository.findById(id).orElse(null));
	}

	public TagUsageApiDto findUsage(Long id)
	{
		TagUsageApiDto tagUsage = new TagUsageApiDto();
		tagUsage.setId(id);

		Tag tag = tagRepository.findById(id).orElse(null);
		if (tag != null)
			tagUsage.setUsage(tag.getDocuments().size());

		return tagUsage;
	}

	private Tag merge(Tag tag, TagDto tagDto)
	{
		if (tag == null || tagDto == null )
			return null;

		tag.setName(tagDto.getName());
		tag.setDescription(tagDto.getDescription());
		tag.setArchived(tagDto.isArchived());

		return tag;
	}

	protected TagDto transform(Tag tag)
	{
		if (tag == null)
			return null;

		TagDto tagDto = new TagDto();

		tagDto.setId(tag.getId());
		tagDto.setName(tag.getName());
		tagDto.setDescription(tag.getDescription());
		tagDto.setArchived(tag.isArchived());

		return tagDto;
	}

	protected Tag transform(TagDto tagDto)
	{
		if (tagDto == null || tagDto.getId() == null)
			return null;

		return tagRepository.findById(tagDto.getId()).orElse(null);
	}
}
