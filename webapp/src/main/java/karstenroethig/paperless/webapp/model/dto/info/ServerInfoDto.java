package karstenroethig.paperless.webapp.model.dto.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerInfoDto
{
	private SystemInfoDto systemInfo;
	private MemoryInfoDto memoryInfo;
}
