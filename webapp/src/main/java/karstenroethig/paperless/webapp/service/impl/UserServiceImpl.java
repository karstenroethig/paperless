package karstenroethig.paperless.webapp.service.impl;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Authority;
import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.repository.UserRepository;
import karstenroethig.paperless.webapp.service.exceptions.AlreadyExistsException;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;

@Service
@Primary
@Transactional
public class UserServiceImpl
{
	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired protected UserRepository userRepository;

	public UserDto create()
	{
		return new UserDto();
	}

	public ValidationResult validate(UserDto user, boolean forInitialCreation)
	{
		ValidationResult result = new ValidationResult();

		if (forInitialCreation && StringUtils.isBlank(user.getPassword()))
			result.addError(MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_EMPTY, "password");

		if (StringUtils.isNotBlank(user.getPassword()) && user.getPassword().length() < 5)
			result.addError(MessageKeyEnum.USER_SAVE_ERROR_PASSWORD_MIN_LENGTH, "password");

		if (!StringUtils.equals(user.getPassword(), user.getRepeatPassword()))
			result.addError(MessageKeyEnum.USER_SAVE_ERROR_REPEAT_PASSWORD_NOT_EQUAL, "repeatPassword");

		return result;
	}

	public UserDto save(UserDto userDto) throws AlreadyExistsException
	{
		doesAlreadyExist(null, userDto.getUsername());

		User user = new User();
		mergeSave(user, userDto);

		return transform(userRepository.save(user));
	}

	public UserDto update(UserDto userDto) throws AlreadyExistsException
	{
		doesAlreadyExist(userDto.getId(), userDto.getUsername());

		User user = userRepository.findById(userDto.getId()).orElse(null);
		mergeUpdate(user, userDto);

		return transform(userRepository.save(user));
	}

	private void doesAlreadyExist(Long id, String username) throws AlreadyExistsException
	{
		User existing = userRepository.findOneByUsernameIgnoreCase(username).orElse(null);
		if (existing != null
				&& (id == null
				|| !existing.getId().equals(id)))
			throw new AlreadyExistsException("username");
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

		return userDto;
	}

	protected User transform(UserDto userDto)
	{
		if (userDto == null || userDto.getId() == null)
			return null;

		return userRepository.findById(userDto.getId()).orElse(null);
	}
}
