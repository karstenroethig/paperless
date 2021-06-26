package karstenroethig.paperless.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public enum ViewEnum
{
	DASHBOARD("/dashboard"),

	ADMIN_SERVER_INFO(ControllerEnum.ADMIN, "/server-info" ),

	USER_LOGIN(ControllerEnum.USER, "/login"),
	USER_LOGOUT_SUCCESS(ControllerEnum.USER, "/logout-success"),
	USER_REGISTER(ControllerEnum.USER, "/register"),
	USER_REGISTER_SUCCESS(ControllerEnum.USER, "/register-success"),
	USER_SHOW(ControllerEnum.USER, ActionEnum.SHOW),
	USER_EDIT(ControllerEnum.USER, ActionEnum.EDIT),
	USER_DELETE_SUCCESS(ControllerEnum.USER, "/delete-success"),

	USER_ADMIN_LIST(ControllerEnum.USER_ADMIN, ActionEnum.LIST),
	USER_ADMIN_SHOW(ControllerEnum.USER_ADMIN, ActionEnum.SHOW),
	USER_ADMIN_CREATE(ControllerEnum.USER_ADMIN, ActionEnum.CREATE),
	USER_ADMIN_EDIT(ControllerEnum.USER_ADMIN, ActionEnum.EDIT),

	GROUP_LIST(ControllerEnum.GROUP, ActionEnum.LIST),
	GROUP_SHOW(ControllerEnum.GROUP, ActionEnum.SHOW),
	GROUP_CREATE(ControllerEnum.GROUP, ActionEnum.CREATE),
	GROUP_EDIT(ControllerEnum.GROUP, ActionEnum.EDIT),

	BACKUP_INDEX(ControllerEnum.BACKUP, "/index"),
	BACKUP_RESTORE(ControllerEnum.BACKUP, "/restore"),
	BACKUP_RESTORE_STATUS(ControllerEnum.BACKUP, "/restore-status"),

	CONTACT_LIST(ControllerEnum.CONTACT, ActionEnum.LIST),
	CONTACT_SHOW(ControllerEnum.CONTACT, ActionEnum.SHOW),
	CONTACT_CREATE(ControllerEnum.CONTACT, ActionEnum.CREATE),
	CONTACT_EDIT(ControllerEnum.CONTACT, ActionEnum.EDIT),

	DOCUMENT_LIST(ControllerEnum.DOCUMENT, ActionEnum.LIST),
	DOCUMENT_SHOW(ControllerEnum.DOCUMENT, ActionEnum.SHOW),
	DOCUMENT_CREATE(ControllerEnum.DOCUMENT, ActionEnum.CREATE),
	DOCUMENT_EDIT(ControllerEnum.DOCUMENT, ActionEnum.EDIT),

	DOCUMENT_BOX_LIST(ControllerEnum.DOCUMENT_BOX, ActionEnum.LIST),
	DOCUMENT_BOX_SHOW(ControllerEnum.DOCUMENT_BOX, ActionEnum.SHOW),
	DOCUMENT_BOX_CREATE(ControllerEnum.DOCUMENT_BOX, ActionEnum.CREATE),
	DOCUMENT_BOX_EDIT(ControllerEnum.DOCUMENT_BOX, ActionEnum.EDIT),

	DOCUMENT_TYPE_LIST(ControllerEnum.DOCUMENT_TYPE, ActionEnum.LIST),
	DOCUMENT_TYPE_SHOW(ControllerEnum.DOCUMENT_TYPE, ActionEnum.SHOW),
	DOCUMENT_TYPE_CREATE(ControllerEnum.DOCUMENT_TYPE, ActionEnum.CREATE),
	DOCUMENT_TYPE_EDIT(ControllerEnum.DOCUMENT_TYPE, ActionEnum.EDIT),

	TAG_LIST(ControllerEnum.TAG, ActionEnum.LIST),
	TAG_SHOW(ControllerEnum.TAG, ActionEnum.SHOW),
	TAG_CREATE(ControllerEnum.TAG, ActionEnum.CREATE),
	TAG_EDIT(ControllerEnum.TAG, ActionEnum.EDIT);

	private static final String VIEW_SUBDIRECTORY = "views";

	@Getter
	private String viewName = StringUtils.EMPTY;

	private enum ControllerEnum
	{
		ADMIN,
		USER_ADMIN("/user-admin"),
		USER,
		GROUP,
		BACKUP,
		CONTACT,
		DOCUMENT,
		DOCUMENT_BOX("/document-box"),
		DOCUMENT_TYPE("/document-type"),
		TAG;

		private String path = null;

		private ControllerEnum() {}

		private ControllerEnum(String path)
		{
			this.path = path;
		}

		public String getPath()
		{
			return path != null ? path : ("/" + name().toLowerCase());
		}
	}

	private enum ActionEnum
	{
		CREATE,
		EDIT,
		LIST,
		SHOW;

		private ActionEnum() {}

		public String getPath()
		{
			return "/" + name().toLowerCase();
		}
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, ActionEnum action)
	{
		this(subController, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, ActionEnum action)
	{
		this(null, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, String path)
	{
		this(null, controller, path);
	}

	private ViewEnum(String path)
	{
		this(null, null, path);
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, String path)
	{
		StringBuilder newViewName = new StringBuilder(VIEW_SUBDIRECTORY);

		if (subController != null)
			newViewName.append(subController.getPath());

		if (controller != null)
			newViewName.append(controller.getPath());

		if (path != null)
			newViewName.append(path);

		viewName = StringUtils.removeStart(newViewName.toString(), "/"); // just in case if there is no view sub-directory
	}
}
