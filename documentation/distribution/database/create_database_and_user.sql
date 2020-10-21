-- Create a database for the application to store the data.
create database paperless_prod;

-- Create a database user which the application will connect as and grant the required permissions.
create user 'paperless_user'@'localhost' identified by 'paperless_password';
grant CREATE, DROP, DELETE, INSERT, SELECT, UPDATE, ALTER, REFERENCES on wodsapp_prod.* to 'paperless_user'@'localhost';
