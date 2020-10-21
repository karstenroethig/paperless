package karstenroethig.paperless.webapp.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Message
{
	@NonNull
	private String key;

	private Object[] params = null;
}
