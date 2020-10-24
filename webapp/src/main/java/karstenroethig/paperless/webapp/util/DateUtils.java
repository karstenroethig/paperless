package karstenroethig.paperless.webapp.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtils
{
	private DateUtils() {}

	public static long daysFromDateUntilNow(LocalDate date)
	{
		return date.until(LocalDate.now(), ChronoUnit.DAYS);
	}
}
