package karstenroethig.paperless.webapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
public class BackupInitializerServiceImpl
{
	@Autowired private BackupServiceImpl backupService;

	@Scheduled(cron = "${app.backup.schedule}")
	public void scheduledBackup()
	{
		log.info("Starting scheduled backup.");
		performBackup();
	}

	public boolean backupNow()
	{
		return performBackup();
	}

	private synchronized boolean performBackup()
	{
		if (backupService.blockBackupProcess())
		{
			backupService.performBackupAsync();
			return true;
		}

		log.warn("The requested creation of a backup was ignored because a backup process is currently running.");
		return false;
	}
}
