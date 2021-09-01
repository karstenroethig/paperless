package karstenroethig.paperless.webapp.model.dto.backup;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class BackupInfoDto extends AbstractProgressInfoDto
{
	public BackupInfoDto()
	{
		super(MessageKeyEnum.BACKUP_TASK_INITIALIZE);
	}

	public void intializeNewBackup()
	{
		intialize(MessageKeyEnum.BACKUP_TASK_INITIALIZE);
	}
}
