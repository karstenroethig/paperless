package karstenroethig.paperless.webapp.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.domain.Tag_;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.TagSearchDto;
import karstenroethig.paperless.webapp.model.dto.api.TagUsageApiDto;
import karstenroethig.paperless.webapp.repository.TagRepository;
import karstenroethig.paperless.webapp.repository.specification.TagSpecifications;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class TagServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Tag_.NAME));

	private static final TagSearchDto EMPTY_SEACH_PARAMS = new TagSearchDto();

	@Autowired private TagRepository tagRepository;

	public TagDto create()
	{
		return new TagDto();
	}

	public ValidationResult validate(TagDto tag)
	{
		ValidationResult result = new ValidationResult();

		if (tag == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(tag));

		return result;
	}

	private void checkValidation(TagDto tag)
	{
		ValidationResult result = validate(tag);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(TagDto tag)
	{
		ValidationResult result = new ValidationResult();

		Tag existing = tagRepository.findOneByNameIgnoreCase(tag.getName()).orElse(null);
		if (existing != null
				&& (tag.getId() == null
				|| !existing.getId().equals(tag.getId())))
			result.addError("name", MessageKeyEnum.TAG_SAVE_ERROR_EXISTS_NAME);

		return result;
	}

	public TagDto save(TagDto tagDto)
	{
		checkValidation(tagDto);

		Tag tag = new Tag();
		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	public TagDto update(TagDto tagDto)
	{
		checkValidation(tagDto);

		Tag tag = tagRepository.findById(tagDto.getId()).orElse(null);
		if (tag == null)
			return null;

		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	public boolean delete(Long id)
	{
		Tag tag = tagRepository.findById(id).orElse(null);
		if (tag == null)
			return false;

		tagRepository.delete(tag);
		return true;
	}

	public long count()
	{
		return tagRepository.count();
	}

	public Page<TagDto> find(TagSearchDto tagSearchDto, Pageable pageable)
	{
		Page<Tag> page = tagRepository.findAll(TagSpecifications.matchesSearchParam(tagSearchDto), pageable);
		return page.map(this::transform);
	}

	public List<TagDto> findAllUnarchived()
	{
		Page<TagDto> pageDto = find(EMPTY_SEACH_PARAMS, ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public Page<TagDto> findAll(Pageable pageable)
	{
		Page<Tag> page = tagRepository.findAll(pageable);
		return page.map(this::transform);
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
		if (tagDto == null)
			return null;

		if (tagDto.getId() != null)
			return tagRepository.findById(tagDto.getId()).orElse(null);

		if (StringUtils.isNotBlank(tagDto.getName()))
			return findOrCreateTag(tagDto.getName());

		return null;
	}

	private Tag findOrCreateTag(String name)
	{
		Tag tag = tagRepository.findOneByNameIgnoreCase(name).orElse(null);

		if (tag != null)
			return tag;

		tag = new Tag();
		tag.setName(name);
		tag.setArchived(Boolean.FALSE);

		return tagRepository.save(tag);
	}
}
