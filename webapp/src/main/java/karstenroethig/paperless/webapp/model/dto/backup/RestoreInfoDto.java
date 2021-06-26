package karstenroethig.paperless.webapp.model.dto.backup;

import java.util.ArrayList;
import java.util.List;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class RestoreInfoDto extends AbstractProgressInfoDto
{
	private boolean error = false;
	private List<String> errorMessages = new ArrayList<>();

	public RestoreInfoDto()
	{
		super(MessageKeyEnum.RESTORE_TASK_INITIALIZE);
	}

	public void intializeNewRestore()
	{
		error = false;
		errorMessages.clear();

		intialize(MessageKeyEnum.RESTORE_TASK_INITIALIZE);
	}

	public void addErrorMessage(String errorMessage)
	{
		error = true;
		errorMessages.add(errorMessage);
	}
}
