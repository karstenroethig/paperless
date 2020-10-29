package karstenroethig.paperless.webapp.controller.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import karstenroethig.paperless.webapp.util.DateUtils;

public class TemplateDateUtils
{
	public static final TemplateDateUtils INSTANCE = new TemplateDateUtils();

	private static final PrettyTime PRETTY_TIME_FORMATTER = new PrettyTime(Locale.ENGLISH);

	private TemplateDateUtils() {}

	public long daysFromDateUntilNow(LocalDate date)
	{
		return DateUtils.daysFromDateUntilNow(date);
	}

	public String prettyFormatDuration(LocalDateTime dateTime)
	{
		if (dateTime == null)
			return null;
		return PRETTY_TIME_FORMATTER.format(DateUtils.asDate(dateTime));
	}
}
