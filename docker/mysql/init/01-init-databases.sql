CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS credential_db;

CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_pass';
CREATE USER IF NOT EXISTS 'product_user'@'%' IDENTIFIED BY 'product_pass';
CREATE USER IF NOT EXISTS 'credential_user'@'%' IDENTIFIED BY 'credential_pass';

GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'product_user'@'%';
GRANT ALL PRIVILEGES ON credential_db.* TO 'credential_user'@'%';

FLUSH PRIVILEGES;
