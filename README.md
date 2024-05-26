
---------
---------
---------
---------




### Executing the program (ONLY FOR LINUX)

Firstly we need to create the server-side.
In order to execute the compilation program for the server we shall give 
execution permissions to the bash file with the next command.

> chmod +x ./maven/bin/mvn

thus we compile the server files.

> ./maven/bin/mvn

The commands to run the server are already in the runserver.sh file, which also need execution permissions

> chmod +x ./runserver.sh

then it is posible to the server just using

> ./runserver.sh

If we do not have any errors, we can go to or browser and type the next direction: 

> http://localhost:8080/

Where the algorithm should be running

