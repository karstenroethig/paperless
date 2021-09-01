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
import karstenroethig.paperless.webapp.model.dto.CommentDto;
import karstenroethig.paperless.webapp.model.dto.ContactDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentDto;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.model.dto.backup.BackupInfoDto;
import karstenroethig.paperless.webapp.model.jaxb.backup.AuthorityXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.CommentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.ContactXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentBoxXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentTypeXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.FileAttachmentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.GroupXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.ObjectFactory;
import karstenroethig.paperless.webapp.model.jaxb.backup.PaperlessXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.TagXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.UserXml;
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

			PaperlessXml paperless = PAPERLESS_BACKUP_OBJECT_FACTORY.createPaperlessXml();
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

	private List<ContactXml> convertContacts()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_CONTACTS, (int)contactService.count());

		List<ContactXml> contacts = null;

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
				ContactXml contact = convertContact(contactDto, true);
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

	private ContactXml convertContact(ContactDto contactDto, boolean full)
	{
		if (contactDto == null)
			return null;

		ContactXml contact = PAPERLESS_BACKUP_OBJECT_FACTORY.createContactXml();
		contact.setId(contactDto.getId());
		contact.setName(contactDto.getName());

		if (full && contactDto.isArchived())
		{
			contact.setArchived(contactDto.isArchived());
		}

		return contact;
	}

	private List<DocumentBoxXml> convertDocumentBoxes()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENT_BOXES, (int)documentBoxService.count());

		List<DocumentBoxXml> documentBoxes = null;

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
				DocumentBoxXml documentBox = convertDocumentBox(documentBoxDto, true);
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

	private DocumentBoxXml convertDocumentBox(DocumentBoxDto documentBoxDto, boolean full)
	{
		if (documentBoxDto == null)
			return null;

		DocumentBoxXml documentBox = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocumentBoxXml();
		documentBox.setId(documentBoxDto.getId());
		documentBox.setName(documentBoxDto.getName());

		if (full)
		{
			documentBox.setDescription(documentBoxDto.getDescription());

			if (documentBoxDto.isArchived())
				documentBox.setArchived(documentBoxDto.isArchived());
		}

		return documentBox;
	}

	private List<DocumentTypeXml> convertDocumentTypes()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENT_TYPES, (int)documentTypeService.count());

		List<DocumentTypeXml> documentTypes = null;

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
				DocumentTypeXml documentType = convertDocumentType(documentTypeDto, true);
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

	private DocumentTypeXml convertDocumentType(DocumentTypeDto documentTypeDto, boolean full)
	{
		if (documentTypeDto == null)
			return null;

		DocumentTypeXml documentType = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocumentTypeXml();
		documentType.setId(documentTypeDto.getId());
		documentType.setName(documentTypeDto.getName());

		if (full)
		{
			documentType.setDescription(documentTypeDto.getDescription());

			if (documentTypeDto.isArchived())
				documentType.setArchived(documentTypeDto.isArchived());
		}

		return documentType;
	}

	private List<TagXml> convertTags()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_TAGS, (int)tagService.count());

		List<TagXml> tags = null;

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
				TagXml tag = convertTag(tagDto, true);
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

	private List<TagXml> convertTags(List<TagDto> tagsDto)
	{
		if (tagsDto == null || tagsDto.isEmpty())
			return null;

		List<TagXml> tags = new ArrayList<>();

		for (TagDto tagDto : tagsDto)
		{
			TagXml tag = convertTag(tagDto, false);
			if (tag != null)
				tags.add(tag);
		}

		return tags;
	}

	private TagXml convertTag(TagDto tagDto, boolean full)
	{
		if (tagDto == null)
			return null;

		TagXml tag = PAPERLESS_BACKUP_OBJECT_FACTORY.createTagXml();
		tag.setId(tagDto.getId());
		tag.setName(tagDto.getName());

		if (full)
		{
			tag.setDescription(tagDto.getDescription());

			if (tagDto.isArchived())
				tag.setArchived(tagDto.isArchived());
		}

		return tag;
	}

	private List<GroupXml> convertGroups()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_GROUPS, (int)groupService.count());

		List<GroupXml> groups = null;

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
				GroupXml group = convertGroup(groupDto);
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

	private List<GroupXml> convertGroups(List<GroupDto> groupsDto)
	{
		if (groupsDto == null || groupsDto.isEmpty())
			return null;

		List<GroupXml> groups = new ArrayList<>();

		for (GroupDto groupDto : groupsDto)
		{
			GroupXml group = convertGroup(groupDto);
			if (group != null)
				groups.add(group);
		}

		return groups;
	}

	private GroupXml convertGroup(GroupDto groupDto)
	{
		if (groupDto == null)
			return null;

		GroupXml group = PAPERLESS_BACKUP_OBJECT_FACTORY.createGroupXml();
		group.setId(groupDto.getId());
		group.setName(groupDto.getName());

		return group;
	}

	private List<UserXml> convertUsers()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_USERS, (int)userService.count());

		List<UserXml> users = null;

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
				UserXml user = PAPERLESS_BACKUP_OBJECT_FACTORY.createUserXml();
				user.setId(userDto.getId());
				user.setUsername(userDto.getUsername());
				user.setHashedPassword(userDto.getHashedPassword());
				user.setFullName(userDto.getFullName());
				user.setEnabled(userDto.isEnabled());

				if (userDto.isLocked())
					user.setLocked(userDto.isLocked());

				if (userDto.getFailedLoginAttempts() > 0)
					user.setFailedLoginAttempts(userDto.getFailedLoginAttempts());

				if (userDto.isNewRegistered())
					user.setNewRegistered(userDto.isNewRegistered());

				if (userDto.isDeleted())
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

	private List<AuthorityXml> convertAuthorities(List<String> authoritiesDto)
	{
		if (authoritiesDto == null || authoritiesDto.isEmpty())
			return null;

		List<AuthorityXml> authorities = new ArrayList<>();

		for (String authorityDto : authoritiesDto)
		{
			AuthorityXml authority = PAPERLESS_BACKUP_OBJECT_FACTORY.createAuthorityXml();
			authority.setName(authorityDto);
			authorities.add(authority);
		}

		return authorities;
	}

	private List<DocumentXml> convertDocumentsAndArchiveFiles(Path backupArchivePath) throws IOException
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_DOCUMENTS, (int)documentService.count());

		List<DocumentXml> documents = null;

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
				DocumentXml document = PAPERLESS_BACKUP_OBJECT_FACTORY.createDocumentXml();
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

	private List<CommentXml> convertComments(List<CommentDto> commentsDto)
	{
		if (commentsDto == null || commentsDto.isEmpty())
			return null;

		List<CommentXml> comments = new ArrayList<>();

		for (CommentDto commentDto : commentsDto)
		{
			CommentXml comment = PAPERLESS_BACKUP_OBJECT_FACTORY.createCommentXml();
			comment.setId(commentDto.getId());
			comment.setText(commentDto.getText());
			comment.setCreatedDatetime(commentDto.getCreatedDatetime());
			comment.setUpdatedDatetime(commentDto.getUpdatedDatetime());
			comment.setDeleted(commentDto.isDeleted());
			comments.add(comment);
		}

		return comments;
	}

	private List<FileAttachmentXml> convertFileAttachments(List<FileAttachmentDto> fileAttachmentsDto, Path backupArchivePath) throws IOException
	{
		if (fileAttachmentsDto == null || fileAttachmentsDto.isEmpty())
			return null;

		List<FileAttachmentXml> fileAttachments = new ArrayList<>();

		for (FileAttachmentDto fileAttachmentDto : fileAttachmentsDto)
		{
			FileAttachmentXml fileAttachment = PAPERLESS_BACKUP_OBJECT_FACTORY.createFileAttachmentXml();
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

	private void writePaperlessToBackupArchive(Path backupArchivePath, PaperlessXml paperless) throws IOException, JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(PaperlessXml.class);
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
