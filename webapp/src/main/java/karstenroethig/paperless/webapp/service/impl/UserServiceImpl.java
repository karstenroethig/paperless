package karstenroethig.paperless.webapp.service.impl;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Authority;
import karstenroethig.paperless.webapp.model.domain.Group;
import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.repository.UserRepository;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Primary
@Transactional
public class UserServiceImpl
{
	@Autowired protected GroupServiceImpl groupService;

	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired protected UserRepository userRepository;

	public UserDto create()
	{
		return new UserDto();
	}

	public ValidationResult validate(UserDto user)
	{
		ValidationResult result = new ValidationResult();

		if (user == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(user));
		result.add(validatePassword(user));

		return result;
	}

	private void checkValidation(UserDto user)
	{
		ValidationResult result = validate(user);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(UserDto user)
	{
		ValidationResult result = new ValidationResult();

		User existing = userRepository.findOneByUsernameIgnoreCase(user.getUsername()).orElse(null);
		if (existing != null
				&& (user.getId() == null
				|| !existing.getId().equals(user.getId())))
			result.addError("username", MessageKeyEnum.USER_SAVE_ERROR_EXISTS_USERNAME);

		return result;
	}

	private ValidationResult validatePassword(UserDto user)
	{
		ValidationResult result = new ValidationResult();

		if (user.getId() == null && StringUtils.isBlank(user.getPassword()))
			result.addError("password", MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_EMPTY);

		if (StringUtils.isNotBlank(user.getPassword()) && user.getPassword().length() < 5)
			result.addError("password", MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_MIN_LENGTH);

		if (!StringUtils.equals(user.getPassword(), user.getRepeatPassword()))
			result.addError("repeatPassword", MessageKeyEnum.USER_SAVE_ERROR_REPEAT_PASSWORD_NOT_EQUAL);

		return result;
	}

	public UserDto save(UserDto userDto)
	{
		checkValidation(userDto);

		User user = new User();
		mergeSave(user, userDto);

		return transform(userRepository.save(user));
	}

	public UserDto update(UserDto userDto)
	{
		checkValidation(userDto);

		User user = userRepository.findById(userDto.getId()).orElse(null);
		if (user == null)
			return null;

		mergeUpdate(user, userDto);

		return transform(userRepository.save(user));
	}

	public boolean delete(Long id)
	{
		User user = userRepository.findById(id).orElse(null);
		if (user == null)
			return false;

		user.setUsername(null);
		user.setHashedPassword(null);
		user.setFullName(null);
		user.setEnabled(Boolean.FALSE);
		user.setDeleted(Boolean.TRUE);

		user.removeAuthoritiesFromUser();
		user.removeGroupsFromUser();

		userRepository.save(user);

		return true;
	}

	public UserDto find(Long id)
	{
		return transform(userRepository.findById(id).orElse(null));
	}

	public UserDto find(String username)
	{
		return transform(userRepository.findOneByUsernameIgnoreCase(username).orElse(null));
	}

	protected User merge(User user, UserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		user.setUsername(userDto.getUsername());
		user.setFullName(userDto.getFullName());

		if (StringUtils.isNotBlank(userDto.getPassword()))
			user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));

//		if (StringUtils.isNotBlank(userDto.getPassword()))
//		{
//			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//			String passwordHash = "{bcrypt}" + passwordEncoder.encode(userDto.getPassword());
//			user.setPassword(passwordHash);
//
//			System.out.println(passwordHash);
//		}

		return user;
	}

	protected User mergeSave(User user, UserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		user.setEnabled(Boolean.FALSE);
		user.setLocked(Boolean.FALSE);
		user.setFailedLoginAttempts(0);
		user.setNewRegisterd(Boolean.TRUE);
		user.setDeleted(Boolean.FALSE);

		return user;
	}

	protected User mergeUpdate(User user, UserDto userDto)
	{
		if (user == null || userDto == null )
			return null;

		merge(user, userDto);

		user.setNewRegisterd(Boolean.FALSE);

		return user;
	}

	protected UserDto transform(User user)
	{
		if (user == null)
			return null;

		UserDto userDto = new UserDto();

		userDto.setId(user.getId());
		userDto.setUsername(user.getUsername());
		userDto.setFullName(user.getFullName());
		userDto.setEnabled(user.isEnabled());
		userDto.setLocked(user.isLocked());
		userDto.setNewRegistered(user.isNewRegisterd());
		userDto.setDeleted(user.isDeleted());

		for (Authority authority : user.getAuthorities())
		{
			userDto.addAuthority(authority.getName());
		}

		for (Group group : user.getGroups())
		{
			GroupDto groupDto = new GroupDto();

			groupDto.setId(group.getId());
			groupDto.setName(group.getName());

			userDto.addGroup(groupDto);
		}

		return userDto;
	}

	protected User transform(UserDto userDto)
	{
		if (userDto == null || userDto.getId() == null)
			return null;

		return userRepository.findById(userDto.getId()).orElse(null);
	}
}
