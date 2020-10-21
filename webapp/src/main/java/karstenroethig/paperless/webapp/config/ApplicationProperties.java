package karstenroethig.paperless.webapp.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
@Getter
@Setter
public class ApplicationProperties
{
	/** TODO Document me! */
	private Path fileDataDirectory = Paths.get("data/files");
}
