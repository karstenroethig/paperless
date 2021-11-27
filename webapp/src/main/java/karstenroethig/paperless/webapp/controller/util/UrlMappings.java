package karstenroethig.paperless.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;

public class UrlMappings
{
	private static final String REDIRECT_PREFIX = "redirect:";

	public static final String CONTROLLER_API = "/api";
	public static final String CONTROLLER_API_VERSION_1_0 = "/1.0";

	public static final String HOME = "/";
	public static final String LOGOUT = "/logout";
	public static final String DASHBOARD = "/dashboard";
	public static final String CONTROLLER_ADMIN = "/admin";

	public static final String CONTROLLER_USER = "/user";
	public static final String CONTROLLER_USER_ADMIN = "/user-admin";
	public static final String CONTROLLER_GROUP = "/group";

	public static final String CONTROLLER_BACKUP = "/backup";

	public static final String CONTROLLER_COMMENT = "/comment";
	public static final String CONTROLLER_CONTACT = "/contact";
	public static final String CONTROLLER_DOCUMENT = "/document";
	public static final String CONTROLLER_DOCUMENT_BOX = "/document-box";
	public static final String CONTROLLER_DOCUMENT_TYPE = "/document-type";
	public static final String CONTROLLER_FILE_ATTACHMENT = "/file-attachment";
	public static final String CONTROLLER_TAG = "/tag";
	public static final String CONTROLLER_ACTIVITY_STREAM = "/activity-stream";

	public static final String ACTION_LIST = "/list";
	public static final String ACTION_SHOW = "/show/{id}";
	public static final String ACTION_CREATE = "/create";
	public static final String ACTION_EDIT = "/edit/{id}";
	public static final String ACTION_DELETE = "/delete/{id}";
	public static final String ACTION_SAVE = "/save";
	public static final String ACTION_UPDATE = "/update";
	public static final String ACTION_SEARCH = "/search";

	private UrlMappings() {}

	public static String redirect(String controllerPath, String actionPath)
	{
		return redirect(controllerPath + actionPath);
	}

	public static String redirectWithId(String controllerPath, String actionPath, Long id)
	{
		String idString = id == null ? StringUtils.EMPTY : id.toString();
		String path = StringUtils.replace(controllerPath + actionPath, "{id}", idString);
		return redirect(path);
	}

	public static String redirect(String path)
	{
		return REDIRECT_PREFIX + path;
	}
}
