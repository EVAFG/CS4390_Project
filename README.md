# CS4390_Project
CS 4390 Computer Networks Group CS4390_Project

## About
This is a TCP-based math server that lets multiple clients connect and send math problems to solve.

## Quick start

compile everything:
```bash
make
```

run the server:
```bash
make server
```

run a client (in another terminal):
```bash
make client
```

clean up compiled files:
```bash
make clean
```

## Protocol
    - `JOIN:<name>`       - connect with a name
    - `CALC:<expression>` - send a math problem (supports "+", "-", "*", "/", "^", "()")
    - `QUIT`              - disconnect
    
server runs on port 6789 by default

## Running manually
```bash
# compile
javac -d out --release 8 -sourcepath src/main src/main/com/computernetworks/project/*/*.java

# run server
java -cp out com.computernetworks.project.Server.ServerMain

# run client (default is localhost:6789)
java -cp out com.computernetworks.project.Client.ClientMain

# run client with custom server
java -cp out com.computernetworks.project.Client.ClientMain <serverIP> <port>
```

## References/Sources:
    - helpful guide: https://cs.lmu.edu/~ray/notes/javanetexamples/
    - for java logging: https://www.loggly.com/ultimate-guide/java-logging-basics/
    - textbook (specifically chapter 2, even more specifically 2.7.2 Socket Programming with TCP)
    
## Notes
    - compiled using "--release 8" for java version 8 compatability
    - BODMAS order used to calculate expressions