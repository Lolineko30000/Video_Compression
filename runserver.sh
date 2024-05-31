
clear;


export MAVEN_OPTS="-Xmx8096m"


#mvn clean install

# Compilador
./maven/bin/mvn

#Corrida del servidor
./maven/bin/mvn exec:java
