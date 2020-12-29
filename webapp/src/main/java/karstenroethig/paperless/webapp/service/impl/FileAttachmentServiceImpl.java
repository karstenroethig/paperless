package karstenroethig.paperless.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.FileAttachment;
import karstenroethig.paperless.webapp.model.domain.FileAttachment_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentDto;
import karstenroethig.paperless.webapp.model.dto.FileAttachmentUploadDto;
import karstenroethig.paperless.webapp.model.dto.FileStorageDto;
import karstenroethig.paperless.webapp.repository.FileAttachmentRepository;
import karstenroethig.paperless.webapp.repository.specification.FileAttachmentSpecifications;
import karstenroethig.paperless.webapp.util.FileIconEnum;
import karstenroethig.paperless.webapp.util.FilesizeUtils;

@Service
@Transactional
public class FileAttachmentServiceImpl
{
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private FileStorageServiceImpl fileStorageService;

	@Autowired private FileAttachmentRepository fileAttachmentRepository;

	public FileAttachmentUploadDto create(DocumentDto document)
	{
		FileAttachmentUploadDto fileAttachmentUpload = new FileAttachmentUploadDto();
		fileAttachmentUpload.setDocument(document);
		return fileAttachmentUpload;
	}

	public List<FileAttachmentDto> save(FileAttachmentUploadDto fileAttachmentUploadDto) throws IOException
	{
		List<FileAttachmentDto> savedFiles = new ArrayList<>();

		Document document = documentService.transform(fileAttachmentUploadDto.getDocument());
		if (document == null)
			return savedFiles;

		for (MultipartFile uploadFile : fileAttachmentUploadDto.getFiles())
		{
			FileStorageDto fileStorage = fileStorageService.saveFile(uploadFile);

			FileAttachment fileAttachment = new FileAttachment();
			fileAttachment.setFileStorage(fileStorageService.transform(fileStorage));
			fileAttachment.setKey(fileStorage.getFileKey());
			fileAttachment.setDocument(document);
			fileAttachment.setName(resolveFileNameFromMultipartFile(uploadFile));
			fileAttachment.setSize(uploadFile.getSize());
			fileAttachment.setContentType(uploadFile.getContentType());
			fileAttachment.setCreatedDatetime(LocalDateTime.now());
			fileAttachment.setUpdatedDatetime(LocalDateTime.now());

			try (InputStream inputStream = uploadFile.getInputStream())
			{
				fileAttachment.setHash(DigestUtils.md5Hex(inputStream));
			}

			savedFiles.add(transform(fileAttachmentRepository.save(fileAttachment)));
		}

		documentService.markUpdated(document.getId());

		return savedFiles;
	}

	public FileAttachmentDto update(FileAttachmentDto fileAttachmentDto)
	{
		FileAttachment fileAttachment = fileAttachmentRepository.findById(fileAttachmentDto.getId()).orElse(null);
		if (fileAttachment == null)
			return null;

		fileAttachment.setName(fileAttachmentDto.getName());
		fileAttachment.setUpdatedDatetime(LocalDateTime.now());

		FileAttachment savedFileAttachment = fileAttachmentRepository.save(fileAttachment);

		documentService.markUpdated(fileAttachment.getDocument().getId());

		return transform(savedFileAttachment);
	}

	public boolean delete(Long id) throws IOException
	{
		FileAttachment fileAttachment = fileAttachmentRepository.findById(id).orElse(null);
		if (fileAttachment == null)
			return false;

		Long documentId = fileAttachment.getDocument().getId();

		fileStorageService.deleteFile(new FileStorageDto(fileAttachment.getFileStorage().getId(), fileAttachment.getKey()));

		fileAttachmentRepository.delete(fileAttachment);

		documentService.markUpdated(documentId);

		return true;
	}

	public List<FileAttachmentDto> findAllByDocument(DocumentDto document)
	{
		return fileAttachmentRepository.findAll(FileAttachmentSpecifications.matchesDocument(document), Sort.by(FileAttachment_.NAME))
				.stream()
				.map(this::transform)
				.collect(Collectors.toList());
	}

	public FileAttachmentDto find(Long id)
	{
		return transform(fileAttachmentRepository.findById(id).orElse(null));
	}

	private FileAttachmentDto transform(FileAttachment fileAttachment)
	{
		if (fileAttachment == null)
			return null;

		FileAttachmentDto fileAttachmentDto = new FileAttachmentDto();

		fileAttachmentDto.setId(fileAttachment.getId());
		fileAttachmentDto.setDocument(documentService.transform(fileAttachment.getDocument()));
		fileAttachmentDto.setName(fileAttachment.getName());
		fileAttachmentDto.setSize(fileAttachment.getSize());
		fileAttachmentDto.setSizeFormatted(FilesizeUtils.formatFilesize(fileAttachment.getSize()));
		fileAttachmentDto.setContentType(fileAttachment.getContentType());
		fileAttachmentDto.setIcon(FileIconEnum.findFileIcon(fileAttachment.getName()));
		fileAttachmentDto.setHash(fileAttachment.getHash());
		fileAttachmentDto.setCreatedDatetime(fileAttachment.getCreatedDatetime());
		fileAttachmentDto.setUpdatedDatetime(fileAttachment.getUpdatedDatetime());
		fileAttachmentDto.setFileStorage(new FileStorageDto(fileAttachment.getFileStorage().getId(), fileAttachment.getKey()));

		return fileAttachmentDto;
	}

	private String resolveFileNameFromMultipartFile(MultipartFile multipartFile)
	{
		String originalFilename = multipartFile.getOriginalFilename();
		if (StringUtils.contains(originalFilename, "/"))
			return StringUtils.substringAfterLast(originalFilename, "/");
		if (StringUtils.contains(originalFilename, "\\"))
			return StringUtils.substringAfterLast(originalFilename, "\\");
		return originalFilename;
	}
}
