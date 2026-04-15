# compiler and runtime settings
JC       = javac
JCFLAGS  = -d out --release 8 -sourcepath src/main  # --release 8 for partner's java 8
JAVA     = java
JFLAGS   = -cp out

# directories where all the code lives
SRC_DIR    = src/main
SERVER_DIR = com/computernetworks/project/Server
CLIENT_DIR = com/computernetworks/project/Client
OUT_DIR    = out

# grab all the java files we need to compile
SERVER_SRC = $(wildcard $(SRC_DIR)/$(SERVER_DIR)/*.java)
CLIENT_SRC = $(wildcard $(SRC_DIR)/$(CLIENT_DIR)/*.java)
ALL_SRC    = $(SERVER_SRC) $(CLIENT_SRC)

.PHONY: all server client clean

# default target - compile everything
all: $(OUT_DIR)
	$(JC) $(JCFLAGS) $(ALL_SRC)

# make the output directory if it doesn't exist yet
$(OUT_DIR):
	mkdir -p $(OUT_DIR)

# compile and run the server
server: all
	$(JAVA) $(JFLAGS) com.computernetworks.project.Server.ServerMain

# compile and run the client
client: all
	$(JAVA) $(JFLAGS) com.computernetworks.project.Client.ClientMain

# nuke all the compiled files
clean:
	rm -rf $(OUT_DIR)
