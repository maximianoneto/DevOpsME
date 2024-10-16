#!/bin/bash

# Inicia o servidor MariaDB em segundo plano
mysqld_safe &

# Aguarda o MariaDB iniciar
sleep 10

# Cria o banco de dados e o usuário
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS dynamicweb;
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON dynamicweb.* TO 'admin'@'%';
FLUSH PRIVILEGES;
EOF

# Inicia sua aplicação Spring Boot
java -jar build/libs/DynamicWeb-0.0.1.jar
