# How-to configure the application as a systemd service

This guide describes how to configure the application as a systemd service.


## Preliminary work

### Check already used ports

	lsof -i -P -n | grep LISTEN

### Determine Init System (see https://wiki.ubuntu.com/SystemdForUpstartUsers)

	ps -p1 | grep systemd && echo systemd || echo upstart

### Install Nano

	yum install nano

### Install Java

	apt install openjdk-11-jdk

### Install MariaDB

* Install application

	sudo yum install mariadb

* Start service

	sudo systemctl start mariadb
	sudo systemctl status mariadb

* Enable service for Auto-start on Boot

	sudo systemctl enable mariadb

* Run the security script

	mysql_secure_installation
	# Set root password? y
	# Remove anonymous users? y
	# Disallow root login remotely? y
	# Remove test database ans access to it? y
	# Reload privilege tables now? y

### Create user and group

	groupadd -r paperless
	# -r, --system              : Create a system group

	useradd -r -g paperless -d /opt/paperless -s /sbin/nologin paperless
	# -r, --system                  : Create a system account (this will not create a home directory)
	# -g, --gid paperless           : The group name or number of the User's initial login group
	# -d, --home-dir /opt/paperless : The user's login directory
	# -s, --shell /sbin/nologin     : Name of the user's login shell (if user logs in, he will get the message from /etc/nologin.txt)

### Create working directory

	mkdir /opt/paperless
	chown -R paperless:paperless /opt/paperless


## Install the application

* Switch to the working directory

	cd /opt/paperless

* Create a new directory for the instance

	mkdir paperless-test

* Copy and extract the installation package

	cd /opt/paperless/[INSTANCE]/
	tar -zxf ~/paperless_v[VERSION].tar.gz

* Rename and edit configuration files

	cd /opt/paperless/[INSTANCE]/bin
	rm -f run.cmd
	rm -f run.conf.bat.example
	chmod +x run.sh
	cd /opt/paperless/[INSTANCE]/config
	mv application.yml.example application.yml
	nano application.yml
	# Set ports
	# Set database connection parameters (URL, user, password)

* First test execution of the application

	cd /opt/paperless/[INSTANCE]
	bash bin/run.sh


## Configure service

	cd /opt/paperless/[INSTANCE]
	chown -R paperless:paperless /opt/paperless/[INSTANCE]
	cp /opt/paperless/[INSTANCE]/bin/systemd/paperless.service /etc/systemd/system/[INSTANCE].service
	nano /etc/systemd/system/[INSTANCE].service
	# 'Requires' -> Set the service for your database server (remove the entire line if it is on a different server)
	# 'After' -> Add the service for your database server
	# 'Before' -> Set the service for your reverse proxy
	# 'WorkingDirectory' -> Set path
	# 'ExecStart' -> Set path
	# 'SyslogIdentifier' -> Set instance
	systemctl daemon-reload`
	systemctl enable [INSTANCE]
	systemctl start [INSTANCE]


## Update application

* Stop running application

	systemctl stop [INSTANCE]

* Copy new artifact

	cp ~/paperless.jar /opt/paperless/[INSTANCE]/lib/

* Update file owner

	chown -R paperless:paperless /opt/paperless/[INSTANCE]

* Start application

	systemctl start [INSTANCE]


## Monitor service

* Check status

	systemctl status [INSTANCE]

* Update after editing the .service file

	systemctl daemon-reload

* Information with the help of the journal

	journalctl -f -u [INSTANCE]
	journalctl -f -u [INSTANCE] --since "1 hour ago"
	journalctl -f -u [INSTANCE] --since "2 days ago"
	journalctl -f -u [INSTANCE] --since "2015-06-26 23:15:00" --until "2015-06-26 23:20:00"
