# How-to set up a reverse proxy with NginX

This guide describes how to set up a reverse proxy with NginX.


## Helpful documentation

* [Configuring NGINX Plus as a Web Server](https://docs.nginx.com/nginx/admin-guide/web-server/web-server/)
* [NGINX Reverse Proxy](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)


## Requirements

* Certificates are required under the following locations:

	/etc/pki/tls/certs/[DOMAIN]/cert.pem
	/etc/pki/tls/certs/[DOMAIN]/fullchain.pem
	/etc/pki/tls/certs/[DOMAIN]/privkey.pem


## Install NginX

* If not already available, install NginX with the command `yum install nginx`


## Provide the error pages

* Copy the error pages directory to `/usr/share/nginx/html/`


## Create a configuration

* Create the file `/etc/nginx/conf.d/[SUBDOMAIN].conf` with the following content:

	server {
		listen 80;
		listen [::]:80;

		server_name [SUBDOMAIN].[DOMAIN];
		# example: server_name my-app.example.com;

		return 301 https://$server_name$request_uri;
	}

	server {
		listen 443 ssl;
		listen [::]:443 ssl;

		server_name [SUBDOMAIN].[DOMAIN];
		# example: server_name my-app.example.com;

		ssl_certificate /etc/pki/tls/certs/[DOMAIN]/fullchain.pem;
		# example: ssl_certificate /etc/pki/tls/certs/example.com/fullchain.pem;
		ssl_certificate_key /etc/pki/tls/certs/[DOMAIN]/privkey.pem;
		# example: ssl_certificate_key /etc/pki/tls/certs/example.com/privkey.pem;

		location / {
			proxy_pass http://127.0.0.1:[APP_PORT];
			# example: proxy_pass http://127.0.0.1:8080;

			proxy_set_header X-Forwarded-Proto $scheme;
			proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
			proxy_set_header X-Real-IP $remote_addr;
			proxy_set_header Host $http_host;

			error_page 400 /ErrorPages/HTTP400.html;
			error_page 401 /ErrorPages/HTTP401.html;
			error_page 402 /ErrorPages/HTTP402.html;
			error_page 403 /ErrorPages/HTTP403.html;
			error_page 404 /ErrorPages/HTTP404.html;
			error_page 500 /ErrorPages/HTTP500.html;
			error_page 501 /ErrorPages/HTTP501.html;
			error_page 502 /ErrorPages/HTTP502.html;
			error_page 503 /ErrorPages/HTTP503.html;
		}

		location /ErrorPages/ {
			alias /usr/share/nginx/html/error_pages/;
			internal;
		}
	}

* Make sure that you have replaced the following place holders with valid values:
** [DOMAIN]
** [SUBDOMAIN]
** [APP_PORT]


## Test configuration

* With the command `nginx -t` the configuration can be tested by nginx


## Resart NginX

* With the command `systemctl restart nginx` NginX is restarted and the changes become active
