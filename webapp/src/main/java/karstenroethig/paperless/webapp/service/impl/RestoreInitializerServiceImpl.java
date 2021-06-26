package karstenroethig.paperless.webapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.dto.backup.RestoreDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
public class RestoreInitializerServiceImpl
{
	@Autowired private RestoreServiceImpl restoreService;

	public synchronized boolean performRestore(RestoreDto restore)
	{
		if (restoreService.blockRestoreProcess())
		{
			restoreService.performRestoreAsync(restore);
			return true;
		}

		log.warn("The requested recovery of data was ignored because a restore process is currently running.");
		return false;
	}
}
