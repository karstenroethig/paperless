package karstenroethig.paperless.webapp.service.impl;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.config.AsyncTaskExecutorConfig;
import karstenroethig.paperless.webapp.model.domain.FileStorage;
import karstenroethig.paperless.webapp.model.dto.backup.RestoreDto;
import karstenroethig.paperless.webapp.model.dto.backup.RestoreInfoDto;
import karstenroethig.paperless.webapp.model.jaxb.backup.ContactXml;
import karstenroethig.paperless.webapp.model.jaxb.backup.PaperlessXml;
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
		try
		{
			checkValidation(restore);

			PaperlessXml paperless = readPaperlessFromBackupFile(restore);

			determineTotalWork(paperless);

			resetDatabase();

			importContacts(paperless.getContacts());

//			TimeUnit.SECONDS.sleep(10l);
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

	private PaperlessXml readPaperlessFromBackupFile(RestoreDto restore) throws IOException, JAXBException
	{
		Path backupFilePath = Paths.get(restore.getFilePath());

		if (FileTypeUtils.isXmlFile(backupFilePath))
			return readPaperlessFromBackupXmlFile(backupFilePath);
		else if (FileTypeUtils.isZipArchive(backupFilePath))
			return readPaperlessFromBackupZipArchive(backupFilePath);

		throw new RuntimeException("invalid backup file format");
	}

	private PaperlessXml readPaperlessFromBackupXmlFile(Path backupXmlFilePath) throws IOException, JAXBException
	{
		try (InputStream in = Files.newInputStream(backupXmlFilePath))
		{
			return readPaperlessFromInputStream(in);
		}
	}

	private PaperlessXml readPaperlessFromBackupZipArchive(Path backupZipArchivePath) throws IOException, JAXBException
	{
		try (FileSystem backupArchiveFileSystem = FileSystems.newFileSystem(backupZipArchivePath, null))
		{
			Path pathToFileInArchive = backupArchiveFileSystem.getPath("/backup.xml");

			try (InputStream in = Files.newInputStream(pathToFileInArchive))
			{
				return readPaperlessFromInputStream(in);
			}
		}
	}

	private PaperlessXml readPaperlessFromInputStream(InputStream in) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(PaperlessXml.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (PaperlessXml)unmarshaller.unmarshal(in);
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
		authorityRepository.deleteAll();

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

		for (ContactXml contact : contacts)
		{
			karstenroethig.paperless.webapp.model.domain.Contact contactDb = new karstenroethig.paperless.webapp.model.domain.Contact();
			contactDb.setId(contact.getId());
			contactDb.setName(contact.getName());
			contactDb.setArchived(contact.isArchived());

			contactRepository.save(contactDb);

			restoreInfo.worked(1);
		}
	}
}
