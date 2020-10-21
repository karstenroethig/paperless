# How-to set up a reverse proxy with Apache

This guide describes how to set up a reverse proxy with Apache.


## Helpful documentation

* [https://wiki.debian.org/Apache](https://wiki.debian.org/Apache)


## Requirements

* Certificates are required under the following locations:

	/etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/cert.pem
	/etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/chain.pem
	/etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/fullchain.pem
	/etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/privkey.pem


## Install Apache

* If not already available, install Apache with the following commands

	sudo apt-get install apache2
	sudo a2enmod proxy_http
	sudo a2enmod proxy_ajp
	sudo a2enmod ssl
	sudo service apache2 restart


## Create a configuration

* Create the file `/etc/apache2/sites-available/[SUBDOMAIN].conf` with the following content:

	<VirtualHost *:80>
		ServerName [SUBDOMAIN].[DOMAIN]
		# example: ServerName my-app.example.com
		ServerAdmin webmaster@localhost

		Redirect / https://[SUBDOMAIN].[DOMAIN]
		# example: Redirect / https://my-app.example.com

		ErrorLog /var/log/apache2/error.log
		LogLevel warn
		CustomLog /var/log/apache2/access.log combined
		ServerSignature On
	</VirtualHost>

* Create the file `/etc/apache2/sites-available/[SUBDOMAIN]-ssl.conf` with the following content:

	<IfModule mod_ssl.c>
		<VirtualHost *:443>
			ServerName [SUBDOMAIN].[DOMAIN]
			# example: ServerName my-app.example.com
			ServerAdmin webmaster@localhost

			ProxyRequests     Off
			ProxyPreserveHost On

			<Proxy *>
				Require all granted
			</Proxy>

			ProxyPass        / ajp://127.0.0.1:[APP_AJP_PORT]/
			ProxyPassReverse / ajp://127.0.0.1:[APP_AJP_PORT]/

			ErrorLog /var/log/apache2/error.log
			LogLevel warn
			CustomLog /var/log/apache2/access.log combined

			SSLCertificateFile /etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/fullchain.pem
			SSLCertificateKeyFile /etc/letsencrypt/live/[SUBDOMAIN].[DOMAIN]/privkey.pem
			Include /etc/letsencrypt/options-ssl-apache.conf
		</VirtualHost>
	</IfModule>

* Make sure that you have replaced the following place holders with valid values:
** [DOMAIN]
** [SUBDOMAIN]
** [APP_AJP_PORT]


## Active configuration and restart Apache

* Execute the following commands to activate the configuration

	sudo a2ensite [SUBDOMAIN].conf
	sudo a2dissite 000-default
	sudo a2ensite [SUBDOMAIN]-ssl.conf
	sudo service apache2 reload

* Sometimes a restart is necessary

	sudo service apache2 restart
