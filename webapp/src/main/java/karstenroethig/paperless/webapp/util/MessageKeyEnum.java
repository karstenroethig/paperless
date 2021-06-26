package karstenroethig.paperless.webapp.util;

import lombok.Getter;

public enum MessageKeyEnum
{
	APPLICATION_VERSION("application.version"),
	APPLICATION_REVISION("application.revision"),
	APPLICATION_BUILD_DATE("application.buildDate"),

	DEFAULT_VALIDATION_OBJECT_CANNOT_BE_EMPTY("default.validation.objectCannotBeEmpty"),

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
	CONTACT_SAVE_ERROR_EXISTS_NAME("contact.save.error.exists.name"),
	CONTACT_UPDATE_INVALID("contact.update.invalid"),
	CONTACT_UPDATE_SUCCESS("contact.update.success"),
	CONTACT_UPDATE_ERROR("contact.update.error"),
	CONTACT_DELETE_INVALID("contact.delete.invalid"),
	CONTACT_DELETE_INVALID_STILL_IN_USE_BY_DOCUMENTS("contact.delete.invalid.stillInUseByDocuments"),
	CONTACT_DELETE_SUCCESS("contact.delete.success"),
	CONTACT_DELETE_ERROR("contact.delete.error"),

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
	DOCUMENT_BOX_SAVE_ERROR_EXISTS_NAME("documentBox.save.error.exists.name"),
	DOCUMENT_BOX_UPDATE_INVALID("documentBox.update.invalid"),
	DOCUMENT_BOX_UPDATE_SUCCESS("documentBox.update.success"),
	DOCUMENT_BOX_UPDATE_ERROR("documentBox.update.error"),
	DOCUMENT_BOX_DELETE_INVALID("documentBox.delete.invalid"),
	DOCUMENT_BOX_DELETE_INVALID_STILL_IN_USE_BY_DOCUMENTS("documentBox.delete.invalid.stillInUseByDocuments"),
	DOCUMENT_BOX_DELETE_SUCCESS("documentBox.delete.success"),
	DOCUMENT_BOX_DELETE_ERROR("documentBox.delete.error"),

	DOCUMENT_TYPE_SAVE_INVALID("documentType.save.invalid"),
	DOCUMENT_TYPE_SAVE_SUCCESS("documentType.save.success"),
	DOCUMENT_TYPE_SAVE_ERROR("documentType.save.error"),
	DOCUMENT_TYPE_SAVE_ERROR_EXISTS_NAME("documentType.save.error.exists.name"),
	DOCUMENT_TYPE_UPDATE_INVALID("documentType.update.invalid"),
	DOCUMENT_TYPE_UPDATE_SUCCESS("documentType.update.success"),
	DOCUMENT_TYPE_UPDATE_ERROR("documentType.update.error"),
	DOCUMENT_TYPE_DELETE_INVALID("documentType.delete.invalid"),
	DOCUMENT_TYPE_DELETE_INVALID_STILL_IN_USE_BY_DOCUMENTS("documentType.delete.invalid.stillInUseByDocuments"),
	DOCUMENT_TYPE_DELETE_SUCCESS("documentType.delete.success"),
	DOCUMENT_TYPE_DELETE_ERROR("documentType.delete.error"),

	FILE_ATTACHMENT_SAVE_INVALID("fileAttachment.save.invalid"),
	FILE_ATTACHMENT_SAVE_SUCCESS("fileAttachment.save.success"),
	FILE_ATTACHMENT_SAVE_ERROR("fileAttachment.save.error"),
	FILE_ATTACHMENT_DELETE_SUCCESS("fileAttachment.delete.success"),
	FILE_ATTACHMENT_DELETE_ERROR("fileAttachment.delete.error"),

	TAG_SAVE_INVALID("tag.save.invalid"),
	TAG_SAVE_SUCCESS("tag.save.success"),
	TAG_SAVE_ERROR("tag.save.error"),
	TAG_SAVE_ERROR_EXISTS_NAME("tag.save.error.exists.name"),
	TAG_UPDATE_INVALID("tag.update.invalid"),
	TAG_UPDATE_SUCCESS("tag.update.success"),
	TAG_UPDATE_ERROR("tag.update.error"),
	TAG_DELETE_SUCCESS("tag.delete.success"),
	TAG_DELETE_ERROR("tag.delete.error"),

	BACKUP_NOW_SUCCESS("backup.now.success"),
	BACKUP_NOW_ERROR("backup.now.error"),

	BACKUP_TASK_INITIALIZE("backup.task.initialize"),
	BACKUP_TASK_EXPORT_CONTACTS("backup.task.exportContacts"),
	BACKUP_TASK_EXPORT_DOCUMENT_BOXES("backup.task.exportDocumentBoxes"),
	BACKUP_TASK_EXPORT_DOCUMENT_TYPES("backup.task.exportDocumentTypes"),
	BACKUP_TASK_EXPORT_TAGS("backup.task.exportTags"),
	BACKUP_TASK_EXPORT_GROUPS("backup.task.exportGroups"),
	BACKUP_TASK_EXPORT_USERS("backup.task.exportUsers"),
	BACKUP_TASK_EXPORT_DOCUMENTS("backup.task.exportDocuments"),

	RESTORE_EXECUTE_SUCCESS("restore.execute.success"),
	RESTORE_EXECUTE_INVALID("restore.execute.invalid"),
	RESTORE_EXECUTE_INVALID_FILE_PATH_DOES_NOT_EXIST("restore.execute.invalid.filePath.doesNotExist"),
	RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_READABLE("restore.execute.invalid.filePath.notReadable"),
	RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_A_FILE("restore.execute.invalid.filePath.notAFile"),
	RESTORE_EXECUTE_ERROR("restore.execute.error"),

	RESTORE_TASK_INITIALIZE("restore.task.initialize"),
	RESTORE_TASK_RESET_DATABASE("restore.task.resetDatabase"),
	RESTORE_TASK_IMPORT_CONTACTS("restore.task.importContacts"),

	USER_SAVE_INVALID("user.save.invalid"),
	USER_SAVE_ERROR("user.save.error"),
	USER_SAVE_ERROR_EXISTS_USERNAME("user.save.error.exists.username"),
	USER_SAVE_ERROR_PASSWORD_EMPTY("user.save.error.passwordEmpty"),
	USER_SAVE_ERROR_PASSWORD_MIN_LENGTH("user.save.error.passwordMinLength"),
	USER_SAVE_ERROR_REPEAT_PASSWORD_NOT_EQUAL("user.save.error.repeatPasswordNotEqual"),
	USER_UPDATE_INVALID("user.update.invalid"),
	USER_UPDATE_SUCCESS("user.update.success"),
	USER_UPDATE_ERROR("user.update.error"),
	USER_DELETE_ERROR("user.delete.error"),

	USER_ADMIN_SAVE_INVALID("userAdmin.save.invalid"),
	USER_ADMIN_SAVE_SUCCESS("userAdmin.save.success"),
	USER_ADMIN_SAVE_ERROR("userAdmin.save.error"),
	USER_ADMIN_SAVE_ERROR_EXISTS_USERNAME("userAdmin.save.error.exists.username"),
	USER_ADMIN_UPDATE_INVALID("userAdmin.update.invalid"),
	USER_ADMIN_UPDATE_SUCCESS("userAdmin.update.success"),
	USER_ADMIN_UPDATE_ERROR("userAdmin.update.error"),
	USER_ADMIN_DELETE_SUCCESS("userAdmin.delete.success"),
	USER_ADMIN_DELETE_ERROR("userAdmin.delete.error"),

	GROUP_SAVE_INVALID("group.save.invalid"),
	GROUP_SAVE_SUCCESS("group.save.success"),
	GROUP_SAVE_ERROR("group.save.error"),
	GROUP_SAVE_ERROR_EXISTS_NAME("group.save.error.exists.name"),
	GROUP_UPDATE_INVALID("group.update.invalid"),
	GROUP_UPDATE_SUCCESS("group.update.success"),
	GROUP_UPDATE_ERROR("group.update.error"),
	GROUP_DELETE_SUCCESS("group.delete.success"),
	GROUP_DELETE_ERROR("group.delete.error");

	@Getter
	private String key;

	private MessageKeyEnum(String key)
	{
		this.key = key;
	}
}
