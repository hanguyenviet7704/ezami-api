-- create the databases
CREATE DATABASE IF NOT EXISTS wordpress;

-- create the users for each database
CREATE USER 'wordpress'@'%' IDENTIFIED BY '12345678aA@';
GRANT CREATE, ALTER, INDEX, LOCK TABLES, REFERENCES, UPDATE, DELETE, DROP, SELECT, INSERT ON `wordpress`.* TO 'wordpress'@'%';

FLUSH PRIVILEGES;
