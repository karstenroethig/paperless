package karstenroethig.paperless.webapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.Comment;
import karstenroethig.paperless.webapp.model.domain.Comment_;
import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.dto.CommentDto;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;
import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.repository.CommentRepository;
import karstenroethig.paperless.webapp.repository.specification.CommentSpecifications;

@Service
@Transactional
public class CommentServiceImpl
{
	@Autowired private DocumentServiceImpl documentService;
	@Autowired private UserServiceImpl userService;

	@Autowired private CommentRepository commentRepository;

	public CommentDto create(DocumentDto document)
	{
		CommentDto comment = new CommentDto();
		comment.setDocument(document);
		return comment;
	}

	public CommentDto save(CommentDto commentDto, UserDto executingUser)
	{
		Document document = documentService.transform(commentDto.getDocument());
		if (document == null)
			return null;

		User author = userService.transform(executingUser);
		if (author == null)
			return null;

		Comment comment = new Comment();
		comment.setDocument(document);
		comment.setText(commentDto.getText());
		comment.setCreatedDatetime(LocalDateTime.now());
		comment.setDeleted(Boolean.FALSE);
		comment.setAuthor(author);
		Comment savedComment = commentRepository.save(comment);

		documentService.markUpdated(document.getId());

		return transform(savedComment);
	}

	public CommentDto update(CommentDto commentDto)
	{
		Comment comment = commentRepository.findById(commentDto.getId()).orElse(null);
		if (comment == null)
			return null;

		comment.setText(commentDto.getText());
		comment.setUpdatedDatetime(LocalDateTime.now());

		Comment savedComment = commentRepository.save(comment);

		documentService.markUpdated(comment.getDocument().getId());

		return transform(savedComment);
	}

	public boolean delete(Long id)
	{
		Comment comment = commentRepository.findById(id).orElse(null);
		if (comment == null)
			return false;

		comment.setDeleted(Boolean.TRUE);
		commentRepository.save(comment);

		documentService.markUpdated(comment.getDocument().getId());

		return true;
	}

	public List<CommentDto> findAllByDocument(DocumentDto document)
	{
		return commentRepository.findAll(CommentSpecifications.matchesDocument(document), Sort.by(Comment_.CREATED_DATETIME))
				.stream()
				.map(this::transform)
				.collect(Collectors.toList());
	}

	public CommentDto find(Long id)
	{
		return transform(commentRepository.findById(id).orElse(null));
	}

	private CommentDto transform(Comment comment)
	{
		if (comment == null)
			return null;

		CommentDto commentDto = new CommentDto();

		commentDto.setId(comment.getId());
		commentDto.setDocument(documentService.transform(comment.getDocument()));
		commentDto.setText(comment.getText());
		commentDto.setCreatedDatetime(comment.getCreatedDatetime());
		commentDto.setUpdatedDatetime(comment.getUpdatedDatetime());
		commentDto.setDeleted(comment.isDeleted());
		commentDto.setAuthor(userService.transform(comment.getAuthor()));
		return commentDto;
	}
}
