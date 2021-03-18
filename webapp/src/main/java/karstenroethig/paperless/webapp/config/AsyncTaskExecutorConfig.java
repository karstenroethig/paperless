package karstenroethig.paperless.webapp.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncTaskExecutorConfig
{
	public static final String BACKUP_TASK_EXECUTOR = "backup";

	@Bean(name = BACKUP_TASK_EXECUTOR)
	public Executor initializeBackupTaskExecutor()
	{
		return new ThreadPoolTaskExecutor();
	}
}
