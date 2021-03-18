package karstenroethig.paperless.webapp.service.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import karstenroethig.paperless.webapp.config.ApplicationProperties;
import karstenroethig.paperless.webapp.model.domain.FileStorage;
import karstenroethig.paperless.webapp.model.dto.FileStorageDto;
import karstenroethig.paperless.webapp.repository.FileStorageRepository;

@Service
@Transactional
public class FileStorageServiceImpl
{
	private static final long FILE_STORAGE_MAX_SIZE = 500000000l;

	private static final String ROOT_PATH_DELIMITER = "/";

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private FileStorageRepository fileStorageRepository;

	public FileStorageDto saveFile(MultipartFile multipartFile) throws IOException
	{
		FileStorage fileStorage = findOrCreateStorage(multipartFile.getSize());

		Path fileStorageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(fileStorage.getKey());
		String fileKey = UUID.randomUUID().toString();

		try (FileSystem fileStorageFileSystem = FileSystems.newFileSystem(fileStorageArchivePath, null))
		{
			Path pathToFileInArchive = fileStorageFileSystem.getPath(ROOT_PATH_DELIMITER + fileKey);
			multipartFile.transferTo(pathToFileInArchive);
		}

		addAndSaveFilesize(fileStorage.getId(), multipartFile.getSize());

		return new FileStorageDto(fileStorage.getId(), fileKey);
	}

	public Resource loadAsResource(FileStorageDto fileStorageDto) throws IOException
	{
		FileStorage fileStorage = fileStorageRepository.findById(fileStorageDto.getStorageId()).orElse(null);
		if (fileStorage == null)
			return null;

		Path fileStorageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(fileStorage.getKey());
		try (FileSystem fileStorageFileSystem = FileSystems.newFileSystem(fileStorageArchivePath, null))
		{
			Path pathToFileInArchive = fileStorageFileSystem.getPath(ROOT_PATH_DELIMITER + fileStorageDto.getFileKey());
			return new ByteArrayResource(Files.readAllBytes(pathToFileInArchive));
		}
	}

	public void deleteFile(FileStorageDto fileStorageDto) throws IOException
	{
		FileStorage fileStorage = fileStorageRepository.findById(fileStorageDto.getStorageId()).orElse(null);
		if (fileStorage == null)
			return;

		Path fileStorageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(fileStorage.getKey());
		long fileSize = 0l;

		try (FileSystem fileStorageFileSystem = FileSystems.newFileSystem(fileStorageArchivePath, null))
		{
			Path pathToFileInArchive = fileStorageFileSystem.getPath(ROOT_PATH_DELIMITER + fileStorageDto.getFileKey());
			fileSize = Files.size(pathToFileInArchive);
			Files.deleteIfExists(pathToFileInArchive);
		}

		subtractAndSaveFilesize(fileStorage.getId(), fileSize);
	}

	protected FileStorage transform(FileStorageDto fileStorageDto)
	{
		if (fileStorageDto == null || fileStorageDto.getStorageId() == null)
			return null;

		return fileStorageRepository.findById(fileStorageDto.getStorageId()).orElse(null);
	}

	private FileStorage findOrCreateStorage(long requiredStorageSpace) throws IOException
	{
		for (FileStorage fileStorage : fileStorageRepository.findBySizeLessThanOrderBySizeAsc(FILE_STORAGE_MAX_SIZE))
		{
			if (isAcceptableStorage(fileStorage, requiredStorageSpace))
				return fileStorage;
		}

		FileStorage fileStorage = new FileStorage();
		fileStorage.setKey(UUID.randomUUID().toString());
		fileStorage.setSize(0l);

		createAndGetStorageArchiveIfItDoesNotExist(fileStorage.getKey());

		return fileStorageRepository.save(fileStorage);
	}

	private boolean isAcceptableStorage(FileStorage fileStorage, long requiredStorageSpace)
	{
		if (fileStorage == null)
			return false;

		long finalFileSize = fileStorage.getSize() + requiredStorageSpace;

		return finalFileSize <= FILE_STORAGE_MAX_SIZE;
	}

	private Path createAndGetStorageArchiveIfItDoesNotExist(String storageKey) throws IOException
	{
		Path fileDataDirectory = createAndGetFileDataDirectory();
		String storageArchiveFilename = buildStorageFilename(storageKey);
		Path storageArchivePath = fileDataDirectory.resolve(storageArchiveFilename);

		if (!Files.exists(storageArchivePath))
		{
			try (ZipOutputStream out = new ZipOutputStream(
					Files.newOutputStream(
							storageArchivePath,
							StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING)))
			{
				out.setLevel(Deflater.NO_COMPRESSION);
				out.closeEntry();
			}
		}

		return storageArchivePath;
	}

	private Path createAndGetFileDataDirectory() throws IOException
	{
		Path fileDataDirectory = applicationProperties.getFileDataDirectory();
		if (!Files.exists(fileDataDirectory))
		{
			Files.createDirectories(fileDataDirectory);
		}

		return fileDataDirectory;
	}

	private String buildStorageFilename(String storageKey)
	{
		StringBuilder filename = new StringBuilder();

		filename.append("documents");

		if (StringUtils.isNotBlank(storageKey))
		{
			filename.append("_");
			filename.append(storageKey);
		}

		filename.append(".zip");

		return filename.toString();
	}

	private void addAndSaveFilesize(Long storageId, long delta)
	{
		FileStorage fileStorage = fileStorageRepository.findById(storageId).orElse(null);
		if (fileStorage == null)
			return;

		fileStorage.setSize(fileStorage.getSize() + delta);
		fileStorageRepository.save(fileStorage);
	}

	private void subtractAndSaveFilesize(Long storageId, long delta)
	{
		FileStorage fileStorage = fileStorageRepository.findById(storageId).orElse(null);
		if (fileStorage == null)
			return;

		long newSize = fileStorage.getSize() - delta;

		if (newSize < 0)
			newSize = 0l;

		fileStorage.setSize(newSize);
		fileStorageRepository.save(fileStorage);
	}
}
