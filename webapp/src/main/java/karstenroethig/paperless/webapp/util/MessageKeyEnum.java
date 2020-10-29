package karstenroethig.paperless.webapp.util;

import lombok.Getter;

public enum MessageKeyEnum
{
	APPLICATION_VERSION("application.version"),
	APPLICATION_REVISION("application.revision"),
	APPLICATION_BUILD_DATE("application.buildDate"),

	COMMENT_SAVE_INVALID("comment.save.invalid"),
	COMMENT_SAVE_SUCCESS("comment.save.success"),
	COMMENT_SAVE_ERROR("comment.save.error"),
	COMMENT_UPDATE_INVALID("comment.update.invalid"),
	COMMENT_UPDATE_SUCCESS("comment.update.success"),
	COMMENT_UPDATE_ERROR("comment.update.error"),
	COMMENT_DELETE_SUCCESS("comment.delete.success"),
	COMMENT_DELETE_ERROR("comment.delete.error"),

	CONTACT_SAVE_INVALID("contact.save.invalid"),
	CONTACT_SAVE_SUCCESS("contact.save.success"),
	CONTACT_SAVE_ERROR("contact.save.error"),
	CONTACT_SAVE_ERROR_EXISTS("contact.save.error.exists"),
	CONTACT_UPDATE_INVALID("contact.update.invalid"),
	CONTACT_UPDATE_SUCCESS("contact.update.success"),
	CONTACT_UPDATE_ERROR("contact.update.error"),
	CONTACT_DELETE_SUCCESS("contact.delete.success"),
	CONTACT_DELETE_ERROR("contact.delete.error"),
	CONTACT_DELETE_ERROR_STILL_IN_USE("contact.delete.error.stillInUse"),

	DOCUMENT_SAVE_INVALID("document.save.invalid"),
	DOCUMENT_SAVE_SUCCESS("document.save.success"),
	DOCUMENT_SAVE_ERROR("document.save.error"),
	DOCUMENT_UPDATE_INVALID("document.update.invalid"),
	DOCUMENT_UPDATE_SUCCESS("document.update.success"),
	DOCUMENT_UPDATE_ERROR("document.update.error"),
	DOCUMENT_DELETE_SUCCESS("document.delete.success"),
	DOCUMENT_DELETE_ERROR("document.delete.error"),

	DOCUMENT_BOX_SAVE_INVALID("documentBox.save.invalid"),
	DOCUMENT_BOX_SAVE_SUCCESS("documentBox.save.success"),
	DOCUMENT_BOX_SAVE_ERROR("documentBox.save.error"),
	DOCUMENT_BOX_SAVE_ERROR_EXISTS("documentBox.save.error.exists"),
	DOCUMENT_BOX_UPDATE_INVALID("documentBox.update.invalid"),
	DOCUMENT_BOX_UPDATE_SUCCESS("documentBox.update.success"),
	DOCUMENT_BOX_UPDATE_ERROR("documentBox.update.error"),
	DOCUMENT_BOX_DELETE_SUCCESS("documentBox.delete.success"),
	DOCUMENT_BOX_DELETE_ERROR("documentBox.delete.error"),
	DOCUMENT_BOX_DELETE_ERROR_STILL_IN_USE("documentBox.delete.error.stillInUse"),

	DOCUMENT_TYPE_SAVE_INVALID("documentType.save.invalid"),
	DOCUMENT_TYPE_SAVE_SUCCESS("documentType.save.success"),
	DOCUMENT_TYPE_SAVE_ERROR("documentType.save.error"),
	DOCUMENT_TYPE_SAVE_ERROR_EXISTS("documentType.save.error.exists"),
	DOCUMENT_TYPE_UPDATE_INVALID("documentType.update.invalid"),
	DOCUMENT_TYPE_UPDATE_SUCCESS("documentType.update.success"),
	DOCUMENT_TYPE_UPDATE_ERROR("documentType.update.error"),
	DOCUMENT_TYPE_DELETE_SUCCESS("documentType.delete.success"),
	DOCUMENT_TYPE_DELETE_ERROR("documentType.delete.error"),
	DOCUMENT_TYPE_DELETE_ERROR_STILL_IN_USE("documentType.delete.error.stillInUse"),

	FILE_ATTACHMENT_SAVE_INVALID("fileAttachment.save.invalid"),
	FILE_ATTACHMENT_SAVE_SUCCESS("fileAttachment.save.success"),
	FILE_ATTACHMENT_SAVE_ERROR("fileAttachment.save.error"),
	FILE_ATTACHMENT_DELETE_SUCCESS("fileAttachment.delete.success"),
	FILE_ATTACHMENT_DELETE_ERROR("fileAttachment.delete.error"),

	TAG_SAVE_INVALID("tag.save.invalid"),
	TAG_SAVE_SUCCESS("tag.save.success"),
	TAG_SAVE_ERROR("tag.save.error"),
	TAG_SAVE_ERROR_EXISTS("tag.save.error.exists"),
	TAG_UPDATE_INVALID("tag.update.invalid"),
	TAG_UPDATE_SUCCESS("tag.update.success"),
	TAG_UPDATE_ERROR("tag.update.error"),
	TAG_DELETE_SUCCESS("tag.delete.success"),
	TAG_DELETE_ERROR("tag.delete.error");

	@Getter
	private String key;

	private MessageKeyEnum(String key)
	{
		this.key = key;
	}
}
