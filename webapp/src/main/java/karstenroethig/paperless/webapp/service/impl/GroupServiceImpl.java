package karstenroethig.paperless.webapp.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Group;
import karstenroethig.paperless.webapp.model.domain.Group_;
import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.repository.GroupRepository;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class GroupServiceImpl
{
	private static final PageRequest ALL_ELEMENTS_PAGE_REQUEST = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Group_.NAME));

	@Autowired private UserServiceImpl userService;

	@Autowired private GroupRepository groupRepository;

	public GroupDto create()
	{
		return new GroupDto();
	}

	public ValidationResult validate(GroupDto group)
	{
		ValidationResult result = new ValidationResult();

		if (group == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(group));

		return result;
	}

	private void checkValidation(GroupDto group)
	{
		ValidationResult result = validate(group);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(GroupDto group)
	{
		ValidationResult result = new ValidationResult();

		Group existing = groupRepository.findOneByNameIgnoreCase(group.getName()).orElse(null);
		if (existing != null
				&& (group.getId() == null
				|| !existing.getId().equals(group.getId())))
			result.addError("name", MessageKeyEnum.GROUP_SAVE_ERROR_EXISTS_NAME);

		return result;
	}

	public GroupDto save(GroupDto groupDto)
	{
		checkValidation(groupDto);

		Group group = new Group();
		merge(group, groupDto);

		return transform(groupRepository.save(group));
	}

	public GroupDto update(GroupDto groupDto)
	{
		checkValidation(groupDto);

		Group group = groupRepository.findById(groupDto.getId()).orElse(null);
		if (group == null)
			return null;

		merge(group, groupDto);

		return transform(groupRepository.save(group));
	}

	public boolean delete(Long id)
	{
		Group group = groupRepository.findById(id).orElse(null);
		if (group == null)
			return false;

		groupRepository.delete(group);
		return true;
	}

	public long count()
	{
		return groupRepository.count();
	}

	public Page<GroupDto> findAll(Pageable pageable)
	{
		Page<Group> page = groupRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public List<GroupDto> findAll()
	{
		Page<GroupDto> pageDto = findAll(ALL_ELEMENTS_PAGE_REQUEST);
		return pageDto.getContent();
	}

	public GroupDto find(Long id)
	{
		return transform(groupRepository.findById(id).orElse(null));
	}

	private Group merge(Group group, GroupDto groupDto)
	{
		if (group == null || groupDto == null )
			return null;

		group.setName(groupDto.getName());

		mergeMembers(group, groupDto);

		return group;
	}

	private Group mergeMembers(Group group, GroupDto groupDto)
	{
		// delete unassigned members
		List<User> previousAssignedMembers = new ArrayList<>(group.getMembers());
		List<Long> newlyAssignedMemberIds = groupDto.getMembers().stream().map(UserDto::getId).collect(Collectors.toList());
		Set<Long> alreadyAssignedMemberIds = new HashSet<>();

		for (User member : previousAssignedMembers)
		{
			if (newlyAssignedMemberIds.contains(member.getId()))
				alreadyAssignedMemberIds.add(member.getId());
			else
				group.removeMember(member);
		}

		// add new assigned members
		for (UserDto member : groupDto.getMembers())
		{
			if (!alreadyAssignedMemberIds.contains(member.getId()))
				group.addMember(userService.transform(member));
		}

		return group;
	}

	private GroupDto transform(Group group)
	{
		if (group == null)
			return null;

		GroupDto groupDto = new GroupDto();

		groupDto.setId(group.getId());
		groupDto.setName(group.getName());

		for (User member : group.getMembers())
		{
			UserDto memberDto = new UserDto();

			memberDto.setId(member.getId());
			memberDto.setUsername(member.getUsername());
			memberDto.setFullName(member.getFullName());

			groupDto.addMember(memberDto);
		}

		return groupDto;
	}

	protected Group transform(GroupDto groupDto)
	{
		if (groupDto == null)
			return null;

		return groupRepository.findById(groupDto.getId()).orElse(null);
	}
}
