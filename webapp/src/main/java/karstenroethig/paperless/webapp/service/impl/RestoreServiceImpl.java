package karstenroethig.paperless.webapp.service.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.config.AsyncTaskExecutorConfig;
import karstenroethig.paperless.webapp.config.SecurityConfiguration.Authorities;
import karstenroethig.paperless.webapp.model.domain.FileStorage;
import karstenroethig.paperless.webapp.model.dto.FileStorageDto;
import karstenroethig.paperless.webapp.model.dto.backup.RestoreDto;
import karstenroethig.paperless.webapp.model.dto.backup.RestoreInfoDto;
import karstenroethig.paperless.webapp.model.jaxb.backup.AuthorityXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.CommentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.ContactXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentBoxXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentTypeXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.DocumentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.FileAttachmentXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.GroupXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.PaperlessXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.TagXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.UserXml;
import karstenroethig.paperless.webapp.repository.AuthorityRepository;
import karstenroethig.paperless.webapp.repository.CommentRepository;
import karstenroethig.paperless.webapp.repository.ContactRepository;
import karstenroethig.paperless.webapp.repository.DocumentBoxRepository;
import karstenroethig.paperless.webapp.repository.DocumentRepository;
import karstenroethig.paperless.webapp.repository.DocumentTypeRepository;
import karstenroethig.paperless.webapp.repository.FileAttachmentRepository;
import karstenroethig.paperless.webapp.repository.FileStorageRepository;
import karstenroethig.paperless.webapp.repository.GroupRepository;
import karstenroethig.paperless.webapp.repository.TagRepository;
import karstenroethig.paperless.webapp.repository.UserRepository;
import karstenroethig.paperless.webapp.util.FileTypeUtils;
import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import karstenroethig.paperless.webapp.util.validation.ValidationException;
import karstenroethig.paperless.webapp.util.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class RestoreServiceImpl
{
	private static RestoreInfoDto restoreInfo = new RestoreInfoDto();

	@Autowired private JdbcTemplate jdbcTemplate;

	@Autowired private ContactRepository contactRepository;
	@Autowired private DocumentBoxRepository documentBoxRepository;
	@Autowired private DocumentTypeRepository documentTypeRepository;
	@Autowired private TagRepository tagRepository;
	@Autowired private DocumentRepository documentRepository;
	@Autowired private CommentRepository commentRepository;
	@Autowired private FileAttachmentRepository fileAttachmentRepository;
	@Autowired private FileStorageRepository fileStorageRepository;
	@Autowired private AuthorityRepository authorityRepository;
	@Autowired private GroupRepository groupRepository;
	@Autowired private UserRepository userRepository;

	@Autowired private FileStorageServiceImpl fileStorageService;
	@Autowired private FileAttachmentServiceImpl fileAttachmentService;

	public RestoreDto create()
	{
		return new RestoreDto();
	}

	public ValidationResult validate(RestoreDto restore)
	{
		ValidationResult result = new ValidationResult();

		if (restore == null)
		{
			result.addError(MessageKeyEnum.DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateFilePath(restore));

		return result;
	}

	private void checkValidation(RestoreDto restore)
	{
		ValidationResult result = validate(restore);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateFilePath(RestoreDto restore)
	{
		ValidationResult result = new ValidationResult();

		Path filePath = Paths.get(restore.getFilePath());
		if (!Files.exists(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_DOES_NOT_EXIST);
		else if (!Files.isReadable(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_READABLE);
		else if (!Files.isRegularFile(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_A_FILE);

		return result;
	}

	public RestoreInfoDto getRestoreInfo()
	{
		return restoreInfo;
	}

	protected boolean blockRestoreProcess()
	{
		if (restoreInfo.isRunning())
			return false;

		restoreInfo.intializeNewRestore();
		return true;
	}

	@Async(AsyncTaskExecutorConfig.BACKUP_TASK_EXECUTOR)
	protected void performRestoreAsync(RestoreDto restore)
	{
		try (IBackupFileSystemWrapper backupFileSystemWrapper = createBackupFileSystemWrapper(restore))
		{
			checkValidation(restore);

			PaperlessXml paperless = readPaperless(backupFileSystemWrapper);

			determineTotalWork(paperless);

			resetDatabase();

			importContacts(paperless.getContacts());
			importDocumentBoxes(paperless.getDocumentBoxes());
			importDocumentTypes(paperless.getDocumentTypes());
			importTags(paperless.getTags());
			importGroups(paperless.getGroups());
			importUsers(paperless.getUsers());
			importDocuments(backupFileSystemWrapper, paperless.getDocuments());
		}
		catch (Exception ex)
		{
			log.error("error on restore process", ex);
			restoreInfo.addErrorMessage(ex.getMessage());
		}
		finally
		{
			restoreInfo.done();
		}
	}

	private IBackupFileSystemWrapper createBackupFileSystemWrapper(RestoreDto restore) throws IOException
	{
		Path backupFilePath = Paths.get(restore.getFilePath());

		if (FileTypeUtils.isXmlFile(backupFilePath))
			return new BackupXmlFileSystemWrapper(backupFilePath);
		else if (FileTypeUtils.isZipArchive(backupFilePath))
			return new BackupZipFileSystemWrapper(backupFilePath);

		throw new IllegalStateException("invalid backup file format");
	}

	private PaperlessXml readPaperless(IBackupFileSystemWrapper backupFileSystemWrapper) throws IOException, JAXBException
	{
		try (InputStream in = backupFileSystemWrapper.getBackupXmlInputStream())
		{
			JAXBContext context = JAXBContext.newInstance(PaperlessXml.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (PaperlessXml)unmarshaller.unmarshal(in);
		}
	}

	private void determineTotalWork(PaperlessXml paperless)
	{
		int totalWork = 0;
		totalWork += paperless.getContacts().size();
		totalWork += paperless.getDocumentBoxes().size();
		totalWork += paperless.getDocumentTypes().size();
		totalWork += paperless.getTags().size();
		totalWork += paperless.getGroups().size();
		totalWork += paperless.getUsers().size();
		totalWork += paperless.getDocuments().size();

		restoreInfo.setTotalWork(totalWork);
	}

	private void resetDatabase() throws IOException
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_RESET_DATABASE, 0);

		commentRepository.deleteAllInBatch();
		fileAttachmentRepository.deleteAllInBatch();

		documentRepository.deleteAllInBatch();
		contactRepository.deleteAllInBatch();
		documentBoxRepository.deleteAllInBatch();
		documentTypeRepository.deleteAllInBatch();
		tagRepository.deleteAllInBatch();

		groupRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();

		List<FileStorage> fileStorages = fileStorageRepository.findAll();
		for (FileStorage fileStorage : fileStorages)
		{
			Path fileStoragePath = fileStorageService.resolvePathToStorageArchive(fileStorage.getKey());
			Files.deleteIfExists(fileStoragePath);
		}

		fileStorageRepository.deleteAllInBatch();
	}

	private void importContacts(List<ContactXml> contacts)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_CONTACTS, contacts.size());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO   contact ");
		sql.append("       (id, name, archived) ");
		sql.append("VALUES (?,  ?,    ?);");

		for (ContactXml contact : contacts)
		{
			jdbcTemplate.update(sql.toString(),
					contact.getId(),
					contact.getName(),
					contact.isArchived());

			restoreInfo.worked(1);
		}
	}

	private void importDocumentBoxes(List<DocumentBoxXml> documentBoxes)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_DOCUMENT_BOXES, documentBoxes.size());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO   document_box ");
		sql.append("       (id, name, description, archived) ");
		sql.append("VALUES (?,  ?,    ?,           ?);");

		for (DocumentBoxXml documentBox : documentBoxes)
		{
			jdbcTemplate.update(sql.toString(),
					documentBox.getId(),
					documentBox.getName(),
					documentBox.getDescription(),
					documentBox.isArchived());

			restoreInfo.worked(1);
		}
	}

	private void importDocumentTypes(List<DocumentTypeXml> documentTypes)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_DOCUMENT_TYPES, documentTypes.size());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO   document_type ");
		sql.append("       (id, name, description, archived) ");
		sql.append("VALUES (?,  ?,    ?,           ?);");

		for (DocumentTypeXml documentType : documentTypes)
		{
			jdbcTemplate.update(sql.toString(),
					documentType.getId(),
					documentType.getName(),
					documentType.getDescription(),
					documentType.isArchived());

			restoreInfo.worked(1);
		}
	}

	private void importTags(List<TagXml> tags)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_TAGS, tags.size());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO   tag ");
		sql.append("       (id, name, description, archived) ");
		sql.append("VALUES (?,  ?,    ?,           ?);");

		for (TagXml tag : tags)
		{
			jdbcTemplate.update(sql.toString(),
					tag.getId(),
					tag.getName(),
					tag.getDescription(),
					tag.isArchived());

			restoreInfo.worked(1);
		}
	}

	private void importGroups(List<GroupXml> groups)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_GROUPS, groups.size());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO   user_group ");
		sql.append("       (id, name) ");
		sql.append("VALUES (?,  ?);");

		for (GroupXml group : groups)
		{
			jdbcTemplate.update(sql.toString(),
					group.getId(),
					group.getName());

			restoreInfo.worked(1);
		}
	}

	private void importUsers(List<UserXml> users)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_USERS, users.size());

		StringBuilder sqlUser = new StringBuilder();
		sqlUser.append("INSERT ");
		sqlUser.append("INTO   user ");
		sqlUser.append("       (id, username, hashed_password, full_name, enabled, locked, failed_login_attempts, new_registered, deleted) ");
		sqlUser.append("VALUES (?,  ?,        ?,               ?,         ?,       ?,      ?,                     ?,              ?);");

		Long authorityIdUser = authorityRepository.findOneByNameIgnoreCase(Authorities.USER).orElseThrow().getId();
		Long authorityIdAdmin = authorityRepository.findOneByNameIgnoreCase(Authorities.ADMIN).orElseThrow().getId();

		StringBuilder sqlUserAuthority = new StringBuilder();
		sqlUserAuthority.append("INSERT ");
		sqlUserAuthority.append("INTO   user_authority ");
		sqlUserAuthority.append("       (user_id, authority_id) ");
		sqlUserAuthority.append("VALUES (?,       ?);");

		StringBuilder sqlUserGroup = new StringBuilder();
		sqlUserGroup.append("INSERT ");
		sqlUserGroup.append("INTO   user_group_member ");
		sqlUserGroup.append("       (user_id, group_id) ");
		sqlUserGroup.append("VALUES (?,       ?);");

		for (UserXml user : users)
		{
			jdbcTemplate.update(sqlUser.toString(),
					user.getId(),
					user.getUsername(),
					user.getHashedPassword(),
					user.getFullName(),
					user.isEnabled(),
					user.isLocked(),
					user.getFailedLoginAttempts(),
					user.isNewRegistered(),
					user.isDeleted());

			for (AuthorityXml authority : user.getAuthorities())
			{
				Long authorityId = null;

				if (StringUtils.equals(authority.getName(), Authorities.ADMIN))
					authorityId = authorityIdAdmin;
				else if (StringUtils.equals(authority.getName(), Authorities.USER))
					authorityId = authorityIdUser;
				else
					continue;

				jdbcTemplate.update(sqlUserAuthority.toString(),
						user.getId(),
						authorityId);
			}

			for (GroupXml group : user.getGroups())
			{
				jdbcTemplate.update(sqlUserGroup.toString(),
						user.getId(),
						group.getId());
			}

			restoreInfo.worked(1);
		}
	}

	private void importDocuments(IBackupFileSystemWrapper backupFileSystemWrapper, List<DocumentXml> documents) throws IOException
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_DOCUMENTS, documents.size());

		StringBuilder sqlDocument = new StringBuilder();
		sqlDocument.append("INSERT ");
		sqlDocument.append("INTO   document ");
		sqlDocument.append("       (id, title, document_type_id, date_of_issue, sender_id, receiver_id, document_box_id, description, ");
		sqlDocument.append("        created_datetime, updated_datetime, review_date, deletion_date, archived) ");
		sqlDocument.append("VALUES (?,  ?,     ?,                ?,             ?,         ?,           ?,               ?, ");
		sqlDocument.append("        ?,                ?,                ?,           ?,             ?);");

		StringBuilder sqlDocumentTag = new StringBuilder();
		sqlDocumentTag.append("INSERT ");
		sqlDocumentTag.append("INTO   document_tag ");
		sqlDocumentTag.append("       (document_id, tag_id) ");
		sqlDocumentTag.append("VALUES (?,           ?);");

		StringBuilder sqlComment = new StringBuilder();
		sqlComment.append("INSERT ");
		sqlComment.append("INTO   comment ");
		sqlComment.append("       (id, document_id, text, created_datetime, updated_datetime, deleted) ");
		sqlComment.append("VALUES (?,  ?,           ?,    ?,                ?,                ?);");

		StringBuilder sqlFileAttachment = new StringBuilder();
		sqlFileAttachment.append("INSERT ");
		sqlFileAttachment.append("INTO   file_attachment ");
		sqlFileAttachment.append("       (id, key, file_storage_id, document_id, name, file_size, content_type, hash, created_datetime, updated_datetime) ");
		sqlFileAttachment.append("VALUES (?,  ?,   ?,               ?,           ?,    ?,         ?,            ?,    ?,                ?);");

		for (DocumentXml document : documents)
		{
			jdbcTemplate.update(sqlDocument.toString(),
					document.getId(),
					document.getTitle(),
					document.getDocumentType().getId(),
					document.getDateOfIssue(),
					document.getSender() != null ? document.getSender().getId() : null,
					document.getReceiver() != null ? document.getReceiver().getId() : null,
					document.getDocumentBox() != null ? document.getDocumentBox().getId() : null,
					document.getDescription(),
					document.getCreatedDatetime(),
					document.getUpdatedDatetime(),
					document.getReviewDate(),
					document.getDeletionDate(),
					document.isArchived());

			for (TagXml tag : document.getTags())
			{
				jdbcTemplate.update(sqlDocumentTag.toString(),
						document.getId(),
						tag.getId());
			}

			for (CommentXml comment : document.getComments())
			{
				jdbcTemplate.update(sqlComment.toString(),
						comment.getId(),
						document.getId(),
						comment.getText(),
						comment.getCreatedDatetime(),
						comment.getUpdatedDatetime(),
						comment.isDeleted());
			}

			for (FileAttachmentXml fileAttachment : document.getFileAttachments())
			{
				long filesize = backupFileSystemWrapper.getFileAttachmentSize(fileAttachment.getPathToFile());
				FileStorageDto fileStorage;

				try (InputStream in = backupFileSystemWrapper.getFileAttachmentInputStream(fileAttachment.getPathToFile()))
				{
					fileStorage = fileStorageService.saveFile(in, filesize);
				}

				jdbcTemplate.update(sqlFileAttachment.toString(),
						fileAttachment.getId(),
						fileStorage.getFileKey(),
						fileStorage.getStorageId(),
						document.getId(),
						fileAttachment.getName(),
						filesize,
						fileAttachment.getContentType(),
						generateFileHash(backupFileSystemWrapper, fileAttachment.getPathToFile()),
						fileAttachment.getCreatedDatetime(),
						fileAttachment.getUpdatedDatetime());
			}

			restoreInfo.worked(1);
		}
	}

	public String generateFileHash(IBackupFileSystemWrapper backupFileSystemWrapper, String relativePathToFile) throws IOException
	{
		try (InputStream inputStream = backupFileSystemWrapper.getFileAttachmentInputStream(relativePathToFile))
		{
			return fileAttachmentService.generateFileHash(inputStream);
		}
	}

	private interface IBackupFileSystemWrapper extends Closeable
	{
		public InputStream getBackupXmlInputStream() throws IOException;

		public long getFileAttachmentSize(String relativePathToFile) throws IOException;

		public InputStream getFileAttachmentInputStream(String relativePathToFile) throws IOException;
	}

	private class BackupXmlFileSystemWrapper implements IBackupFileSystemWrapper
	{
		private final Path backupXmlFilePath;
		private final Path backupXmlDirectoryPath;

		public BackupXmlFileSystemWrapper(Path backupXmlFilePath)
		{
			this.backupXmlFilePath = backupXmlFilePath;
			this.backupXmlDirectoryPath = backupXmlFilePath.getParent();
		}

		private Path resolveFile(String relativePathToFile)
		{
			return backupXmlDirectoryPath.resolve(relativePathToFile);
		}

		@Override
		public void close() throws IOException
		{
			// Nothing to do
		}

		@Override
		public InputStream getBackupXmlInputStream() throws IOException
		{
			return Files.newInputStream(backupXmlFilePath);
		}

		@Override
		public long getFileAttachmentSize(String relativePathToFile) throws IOException
		{
			Path fileAttachmentPath = resolveFile(relativePathToFile);
			return Files.size(fileAttachmentPath);
		}

		@Override
		public InputStream getFileAttachmentInputStream(String relativePathToFile) throws IOException
		{
			Path fileAttachmentPath = resolveFile(relativePathToFile);
			return Files.newInputStream(fileAttachmentPath);
		}
	}

	private class BackupZipFileSystemWrapper implements IBackupFileSystemWrapper
	{
		private final FileSystem backupArchiveFileSystem;

		public BackupZipFileSystemWrapper(Path backupZipArchivePath) throws IOException
		{
			backupArchiveFileSystem = FileSystems.newFileSystem(backupZipArchivePath, null);
		}

		private Path resolveFile(String relativePathToFile)
		{
			return backupArchiveFileSystem.getPath("/" + relativePathToFile);
		}

		@Override
		public void close() throws IOException
		{
			if (backupArchiveFileSystem == null)
				return;
			backupArchiveFileSystem.close();
		}

		@Override
		public InputStream getBackupXmlInputStream() throws IOException
		{
			Path pathToFileInArchive = resolveFile("backup.xml");
			return Files.newInputStream(pathToFileInArchive);
		}

		@Override
		public long getFileAttachmentSize(String relativePathToFile) throws IOException
		{
			Path pathToFileInArchive = resolveFile(relativePathToFile);
			return Files.size(pathToFileInArchive);
		}

		@Override
		public InputStream getFileAttachmentInputStream(String relativePathToFile) throws IOException
		{
			Path pathToFileInArchive = resolveFile(relativePathToFile);
			return Files.newInputStream(pathToFileInArchive);
		}
	}
}
