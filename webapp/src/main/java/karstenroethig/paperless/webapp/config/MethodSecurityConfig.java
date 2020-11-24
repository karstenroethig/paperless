package karstenroethig.paperless.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(
	prePostEnabled = false, // enables Spring Security pre/post annotations
	securedEnabled = true, // determines if the @Secured annotation should be enabled
	jsr250Enabled = false) // allows to use the @RoleAllowed annotation
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration
{
}
