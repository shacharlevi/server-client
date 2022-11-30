1. how to run code:
1.1 command for server 
	a.for baseserver :
		mvn clean
		mvn compile
		mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.TCPMain" -Dexec.args="7777"
	b.for reactor:
        mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.ReactorMain" -Dexec.args="7777 5"
	c.for client
		make
		./bin/BGSclient 127.0.0.1 7777
1.2 examle for each message
	- REGISTER <name> <password> <dd-mm-yyy>
	  example: REGISTER Morty a321 10-11-1999
	- LOGIN <name> <password> <0/1>
	  example: LOGIN Morty a321 1
	- LOGOUT
	- FOLLOW <0/1> <name>
	  example: FOLLOW 0 Rick
	- POST <contant>
	  example:POST bla bla bla
	- PM <name> <contant>
	  example:PM Rick bla bla bla 
	- LOGSTAT 
	- STAT <name name>
	  example: STAT Rick Bird-person
	- BLOCK <name>
	  example: BLOCK Rick
2. inside the class database
