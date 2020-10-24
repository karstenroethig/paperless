package karstenroethig.paperless.webapp.controller.util;

import java.time.LocalDate;

public class DateUtils
{
	public static final DateUtils INSTANCE = new DateUtils();

	private DateUtils() {}

	public long daysFromDateUntilNow(LocalDate date)
	{
		return karstenroethig.paperless.webapp.util.DateUtils.daysFromDateUntilNow(date);
	}
}
