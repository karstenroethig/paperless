package karstenroethig.paperless.webapp.model.dto.backup;

import java.time.LocalDateTime;

import karstenroethig.paperless.webapp.util.MessageKeyEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AbstractProgressInfoDto
{
	private boolean running;
	private LocalDateTime startedDatetime;

	@Setter
	private int totalWork;
	private int worked;

	private MessageKeyEnum taskMessageKey;
	private int taskTotalWork;
	private int taskWorked;

	protected AbstractProgressInfoDto(MessageKeyEnum messageKey)
	{
		intialize(messageKey);
		running = false;
	}

	protected void intialize(MessageKeyEnum messageKey)
	{
		running = true;
		startedDatetime = LocalDateTime.now();

		totalWork = 0;
		worked = 0;

		beginTask(messageKey, 0);
	}

	public void beginTask(MessageKeyEnum messageKey, int totalWork)
	{
		this.taskMessageKey = messageKey;
		this.taskTotalWork = totalWork;
		this.taskWorked = 0;
	}

	public void worked(int worked)
	{
		this.worked += worked;
		this.taskWorked += worked;
	}

	public void done()
	{
		this.running = false;
	}

	public int calcProgressPercentage()
	{
		if (totalWork <= 0)
			return 0;
		return (int)(1.0 * worked / totalWork * 100);
	}
}
