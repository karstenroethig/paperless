package karstenroethig.paperless.webapp.bean.search;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import karstenroethig.paperless.webapp.model.dto.search.DocumentBoxSearchDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DocumentBoxSearchBean
{
	private DocumentBoxSearchDto documentBoxSearchDto;

	@PostConstruct
	public void clear()
	{
		documentBoxSearchDto = new DocumentBoxSearchDto();
	}
}
