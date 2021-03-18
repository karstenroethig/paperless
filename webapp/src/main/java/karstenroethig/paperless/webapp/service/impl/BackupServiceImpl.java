package karstenroethig.paperless.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.config.ApplicationProperties;
import karstenroethig.paperless.webapp.config.ApplicationProperties.BackupSettings;
import karstenroethig.paperless.webapp.config.AsyncTaskExecutorConfig;
import karstenroethig.paperless.webapp.model.domain.AbstractEntityId_;
import karstenroethig.paperless.webapp.model.dto.BackupInfoDto;
import karstenroethig.paperless.webapp.model.dto.CommentDto;
import karstenroethig.paperless.webapp.model.dto.ContactDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentDto;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.model.jaxb.backup.Authority;
import karstenroethig.paperless.webapp.model.jaxb.backup.Comment;
import karstenroethig.paperless.webapp.model.jaxb.backup.Contact;
import karstenroethig.paperless.webapp.model.jaxb.backup.Document;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentBox;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentType;
import karstenroethig.paperless.webapp.model.jaxb.backup.FileAttachment;
import karstenroethig.paperless.webapp.model.jaxb.backup.Group;
import karstenroethig.paperless.webapp.model.jaxb.backup.ObjectFactory;
import karstenroethig.paperless.webapp.model.jaxb.backup.Paperless;
import karstenroethig.paperless.webapp.model.jaxb.backup.Tag;
import karstenroethig.paperless.webapp.model.jaxb.backup.User;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class BackupServiceImpl
{
	private static final ObjectFactory PAPERLESS_BACKUP_OBJECT_FACTORY = new ObjectFactory();

	private static BackupInfoDto backupInfo = new BackupInfoDto();

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private ContactServiceImpl contactService;
	@Autowired private DocumentBoxServiceImpl documentBoxService;
	@Autowired private DocumentTypeServiceImpl documentTypeService;
	@Autowired private TagServiceImpl tagService;
	@Autowired private GroupServiceImpl groupService;
	@Autowired private UserAdminServiceImpl userService;
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private CommentServiceImpl commentService;
	@Autowired private FileAttachmentServiceImpl fileAttachmentService;
	@Autowired private FileStorageServiceImpl fileStorageService;

	public BackupInfoDto getBackupInfo()
	{
		return backupInfo;
	}

	protected boolean blockBackupProcess()
	{
		if (backupInfo.isRunning())
			return false;

		backupInfo.intializeNewBackup();
		return true;
	}

	@Async(AsyncTaskExecutorConfig.BACKUP_TASK_EXECUTOR)
	protected void performBackupAsync()
	{
		Path backupArchivePath = null;

		try
		{
			determineTotalWork();

			backupArchivePath = createBackupArchive();

			Paperless paperless = PAPERLESS_BACKUP_OBJECT_FACTORY.createPaperless();
			paperless.setContacts(convertContacts());
			paperless.setDocumentBoxes(convertDocumentBoxes());
			paperless.setDocumentTypes(convertDocumentTypes());
			paperless.setTags(convertTags());
			paperless.setGroups(convertGroups());
			paperless.setUsers(convertUsers());
			paperless.setDocuments(convertDocumentsAndArchiveFiles(backupArchivePath));

			writePaperlessToBackupArchive(backupArchivePath, paperless);
		}
		catch (Exception ex)
		{
			log.error("error on backup process", ex);

			if (backupArchivePath != null)
			{
				try
				{
					Files.delete(backupArchivePath);
				}
				catch (Exception e)
				{
					log.error("error deleting corrupt backup archive", e);
				}
			}
		}
		finally
		{
			backupInfo.done();
		}
	}

	private void determineTotalWork()
	{
		int totalWork = 0;
		totalWork += contactService.count();
		totalWork += documentBoxService.count();
		totalWork += documentTypeService.count();
		totalWork += tagService.count();
		totalWork += groupService.count();
		totalWork += userService.count();
		totalWork += documentService.count();

		backupInfo.setTotalWork(totalWork);
	}

	private Path createBackupArchive() throws IOException
	{
		BackupSettings backupSettings = applicationProperties.getBackup();
		Path backupDirectoryPath = backupSettings.getBackupDirectory();
		Files.createDirectories(backupDirectoryPath);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(backupSettings.getBackupFileDatePattern());
		String backupFilenameDate = backupInfo.getStartedDatetime().format(dateTimeFormatter);
		String backupFilename = String.format("%s%s.zip", backupSettings.getBackupFilePrefix(), backupFilenameDate);
		Path backupArchivePath = backupDirectoryPath.resolve(backupFilename);

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						backupArchivePath,
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.setLevel(Deflater.NO_COMPRESSION);
			out.closeEntry();
		}

		return backupArchivePath;
	}

	private List<Contact> convertContacts()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_CONTACTS, (int)contactService.count());

		List<Contact> contacts = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<ContactDto> page = contactService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (contacts == null)
				contacts = new ArrayList<>();

			for (ContactDto contactDto : page.getContent())
			{
				Contact contact = convertContact(contactDto, true);
				if (contact != null)
					contacts.add(contact);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return contacts;
	}

	private Contact convertContact(ContactDto contactDto, boolean full)
	{
		if (contactDto == null)
			return null;

		Contact contact = PAPERLESS_BACKUP_OBJECT_FACTORY.createContact();
		contact.setId(contactDto.getId());
		contact.setName(contactDto.getName());

		if (full)
		{
			contact.setArchived(contactDto.isArchived());
		}

		return contact;
	}

	private List<DocumentBox> convertDocumentBoxes()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENT_BOXES, (int)documentBoxService.count());

		List<DocumentBox> documentBoxes = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<DocumentBoxDto> page = documentBoxService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (documentBoxes == null)
				documentBoxes = new ArrayList<>();

			for (DocumentBoxDto documentBoxDto : page.getContent())
			{
				DocumentBox documentBox = convertDocumentBox(documentBoxDto, true);
				if (documentBox != null)
					documentBoxes.add(documentBox);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return documentBoxes;
	}

	private DocumentBox convertDocumentBox(DocumentBoxDto documentBoxDto, boolean full)
	{
		if (documentBoxDto == null)
			return null;

		DocumentBox documentBox = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocumentBox();
		documentBox.setId(documentBoxDto.getId());
		documentBox.setName(documentBoxDto.getName());

		if (full)
		{
			documentBox.setDescription(documentBoxDto.getDescription());
			documentBox.setArchived(documentBoxDto.isArchived());
		}

		return documentBox;
	}

	private List<DocumentType> convertDocumentTypes()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENT_TYPES, (int)documentTypeService.count());

		List<DocumentType> documentTypes = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<DocumentTypeDto> page = documentTypeService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (documentTypes == null)
				documentTypes = new ArrayList<>();

			for (DocumentTypeDto documentTypeDto : page.getContent())
			{
				DocumentType documentType = convertDocumentType(documentTypeDto, true);
				if (documentType != null)
					documentTypes.add(documentType);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return documentTypes;
	}

	private DocumentType convertDocumentType(DocumentTypeDto documentTypeDto, boolean full)
	{
		if (documentTypeDto == null)
			return null;

		DocumentType documentType = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocumentType();
		documentType.setId(documentTypeDto.getId());
		documentType.setName(documentTypeDto.getName());

		if (full)
		{
			documentType.setDescription(documentTypeDto.getDescription());
			documentType.setArchived(documentTypeDto.isArchived());
		}

		return documentType;
	}

	private List<Tag> convertTags()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_TAGS, (int)tagService.count());

		List<Tag> tags = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<TagDto> page = tagService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (tags == null)
				tags = new ArrayList<>();

			for (TagDto tagDto : page.getContent())
			{
				Tag tag = convertTag(tagDto, true);
				if (tag != null)
					tags.add(tag);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return tags;
	}

	private List<Tag> convertTags(List<TagDto> tagsDto)
	{
		if (tagsDto == null || tagsDto.isEmpty())
			return null;

		List<Tag> tags = new ArrayList<>();

		for (TagDto tagDto : tagsDto)
		{
			Tag tag = convertTag(tagDto, false);
			if (tag != null)
				tags.add(tag);
		}

		return tags;
	}

	private Tag convertTag(TagDto tagDto, boolean full)
	{
		if (tagDto == null)
			return null;

		Tag tag = PAPERLESS_BACKUP_OBJECT_FACTORY.createTag();
		tag.setId(tagDto.getId());
		tag.setName(tagDto.getName());

		if (full)
		{
			tag.setDescription(tagDto.getDescription());
			tag.setArchived(tagDto.isArchived());
		}

		return tag;
	}

	private List<Group> convertGroups()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_GROUPS, (int)groupService.count());

		List<Group> groups = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<GroupDto> page = groupService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (groups == null)
				groups = new ArrayList<>();

			for (GroupDto groupDto : page.getContent())
			{
				Group group = convertGroup(groupDto);
				if (group != null)
					groups.add(group);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return groups;
	}

	private List<Group> convertGroups(List<GroupDto> groupsDto)
	{
		if (groupsDto == null || groupsDto.isEmpty())
			return null;

		List<Group> groups = new ArrayList<>();

		for (GroupDto groupDto : groupsDto)
		{
			Group group = convertGroup(groupDto);
			if (group != null)
				groups.add(group);
		}

		return groups;
	}

	private Group convertGroup(GroupDto groupDto)
	{
		if (groupDto == null)
			return null;

		Group group = PAPERLESS_BACKUP_OBJECT_FACTORY.createGroup();
		group.setId(groupDto.getId());
		group.setName(groupDto.getName());

		return group;
	}

	private List<User> convertUsers()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_USERS, (int)userService.count());

		List<User> users = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<UserDto> page = userService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (users == null)
				users = new ArrayList<>();

			for (UserDto userDto : page.getContent())
			{
				User user = PAPERLESS_BACKUP_OBJECT_FACTORY.createUser();
				user.setId(userDto.getId());
				user.setUsername(userDto.getUsername());
				user.setHashedPassword(userDto.getHashedPassword());
				user.setFullName(userDto.getFullName());
				user.setEnabled(userDto.isEnabled());
				user.setLocked(userDto.isLocked());
				user.setNewRegistered(userDto.isNewRegistered());
				user.setDeleted(userDto.isDeleted());
				user.setAuthorities(convertAuthorities(userDto.getAuthorities()));
				user.setGroups(convertGroups(userDto.getGroups()));
				users.add(user);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return users;
	}

	private List<Authority> convertAuthorities(List<String> authoritiesDto)
	{
		if (authoritiesDto == null || authoritiesDto.isEmpty())
			return null;

		List<Authority> authorities = new ArrayList<>();

		for (String authorityDto : authoritiesDto)
		{
			Authority authority = PAPERLESS_BACKUP_OBJECT_FACTORY.createAuthority();
			authority.setName(authorityDto);
			authorities.add(authority);
		}

		return authorities;
	}

	private List<Document> convertDocumentsAndArchiveFiles(Path backupArchivePath) throws IOException
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENTS, (int)documentService.count());

		List<Document> documents = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<DocumentDto> page = documentService.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (documents == null)
				documents = new ArrayList<>();

			for (DocumentDto documentDto : page.getContent())
			{
				Document document = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocument();
				document.setId(documentDto.getId());
				document.setTitle(documentDto.getTitle());
				document.setDocumentType(convertDocumentType(documentDto.getDocumentType(), false));
				document.setDateOfIssue(documentDto.getDateOfIssue());
				document.setSender(convertContact(documentDto.getSender(), false));
				document.setReceiver(convertContact(documentDto.getReceiver(), false));
				document.setTags(convertTags(documentDto.getTags()));
				document.setDocumentBox(convertDocumentBox(documentDto.getDocumentBox(), false));
				document.setDescription(documentDto.getDescription());
				document.setCreatedDatetime(documentDto.getCreatedDatetime());
				document.setUpdatedDatetime(documentDto.getUpdatedDatetime());
				document.setReviewDate(documentDto.getReviewDate());
				document.setDeletionDate(documentDto.getDeletionDate());
				document.setArchived(documentDto.isArchived());
				document.setComments(convertComments(commentService.findAllByDocument(documentDto)));
				document.setFileAttachments(convertFileAttachments(fileAttachmentService.findAllByDocument(documentDto), backupArchivePath));
				documents.add(document);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return documents;
	}

	private List<Comment> convertComments(List<CommentDto> commentsDto)
	{
		if (commentsDto == null || commentsDto.isEmpty())
			return null;

		List<Comment> comments = new ArrayList<>();

		for (CommentDto commentDto : commentsDto)
		{
			Comment comment = PAPERLESS_BACKUP_OBJECT_FACTORY.createComment();
			comment.setId(commentDto.getId());
			comment.setText(commentDto.getText());
			comment.setCreatedDatetime(commentDto.getCreatedDatetime());
			comment.setUpdatedDatetime(commentDto.getUpdatedDatetime());
			comment.setDeleted(commentDto.isDeleted());
			comments.add(comment);
		}

		return comments;
	}

	private List<FileAttachment> convertFileAttachments(List<FileAttachmentDto> fileAttachmentsDto, Path backupArchivePath) throws IOException
	{
		if (fileAttachmentsDto == null || fileAttachmentsDto.isEmpty())
			return null;

		List<FileAttachment> fileAttachments = new ArrayList<>();

		for (FileAttachmentDto fileAttachmentDto : fileAttachmentsDto)
		{
			FileAttachment fileAttachment = PAPERLESS_BACKUP_OBJECT_FACTORY.createFileAttachment();
			fileAttachment.setId(fileAttachmentDto.getId());
			fileAttachment.setName(fileAttachmentDto.getName());
			fileAttachment.setContentType(fileAttachmentDto.getContentType());
			fileAttachment.setCreatedDatetime(fileAttachmentDto.getCreatedDatetime());
			fileAttachment.setUpdatedDatetime(fileAttachmentDto.getUpdatedDatetime());
			fileAttachment.setPathToFile(writeFileAttachmentToBackupArchive(backupArchivePath, fileAttachmentDto));
			fileAttachments.add(fileAttachment);
		}

		return fileAttachments;
	}

	private String writeFileAttachmentToBackupArchive(Path backupArchivePath, FileAttachmentDto fileAttachment) throws IOException
	{
		String stringPathToFileInArchive = String.format("files/%s_%s", fileAttachment.getId(), fileAttachment.getName());

		try (FileSystem backupArchiveFileSystem = FileSystems.newFileSystem(backupArchivePath, null))
		{
			Path pathToFilesDirectoryInArchive = backupArchiveFileSystem.getPath("/files");
			if (!Files.exists(pathToFilesDirectoryInArchive))
				Files.createDirectories(pathToFilesDirectoryInArchive);

			Path pathToFileInArchive = backupArchiveFileSystem.getPath("/" + stringPathToFileInArchive);
			Resource fileResource = fileStorageService.loadAsResource(fileAttachment.getFileStorage());

			try (InputStream in = fileResource.getInputStream();
					OutputStream out = Files.newOutputStream(pathToFileInArchive);)
			{
				IOUtils.copy(in, out);
			}
		}

		return stringPathToFileInArchive;
	}

	private void writePaperlessToBackupArchive(Path backupArchivePath, Paperless paperless) throws IOException, JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(Paperless.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		try (FileSystem backupArchiveFileSystem = FileSystems.newFileSystem(backupArchivePath, null))
		{
			Path pathToFileInArchive = backupArchiveFileSystem.getPath("/backup.xml");

			try (OutputStream out = Files.newOutputStream(pathToFileInArchive))
			{
				marshaller.marshal(paperless, out);
			}
		}
	}
}
