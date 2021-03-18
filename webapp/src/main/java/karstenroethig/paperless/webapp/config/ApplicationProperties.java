package karstenroethig.paperless.webapp.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
@Getter
@Setter
public class ApplicationProperties
{
	/** Directory for storing files */
	private Path fileDataDirectory = Paths.get("data/files");

	/** Backup settings. */
	private BackupSettings backup;

	@Getter
	@Setter
	@ToString
	public static class BackupSettings
	{
		/** Enable backups. */
		private boolean enabled = true;

		/**
		 * Schedule cron expression.
		 * <p>The pattern is a list of six single space-separated fields: representing
		 * second, minute, hour, day, month, weekday. Month and weekday names can be
		 * given as the first three letters of the English names.
		 *
		 * <p>Example patterns:
		 * <ul>
		 * <li>"0 0 * * * *" = the top of every hour of every day.</li>
		 * <li>"*&#47;10 * * * * *" = every ten seconds.</li>
		 * <li>"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.</li>
		 * <li>"0 0 6,19 * * *" = 6:00 AM and 7:00 PM every day.</li>
		 * <li>"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.</li>
		 * <li>"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays</li>
		 * <li>"0 0 0 25 12 ?" = every Christmas Day at midnight</li>
		 * </ul>
		 */
		private String schedule = "0 0 1 * * *";

		/** Directory for backup files. */
		private Path backupDirectory = Paths.get("backups");

		/** Backup file prefix. */
		private String backupFilePrefix = "backup_";

		/** Backup file date pattern. */
		private String backupFileDatePattern = "yyyy-MM-dd_HH-mm";
	}
}
