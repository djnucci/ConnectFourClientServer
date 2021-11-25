## Usage:
	To complile: javac MTServer.java ConnectFourClient.java

#### Compile and run "MTServer.java" and "ConnectFourClient.java" in seperate terminals (WSL tested and prefered), README.txt should also be present (please run with JavaSE-13).
	
## Description:
#### This client / server program is programmed for assignment #1 for SOFE 4790. The server and client enable two players (both in socketed connections) to connect and play a game of connect four through the terminal. The server handles all gameplay and rending of the terminal, the client only deals with user I/O.

## MTServer.java:
	To run: Java MTServer
	
### Options:
#### '-v' or '-V': Verbose mode
	Enables error print logs on the server side.
			
#### '-u' or '-U': Usage
	Prints this file to the terminal.
		
### Description:
#### Start this server to enable up to 20 people (dev changable) to connect to a connect 4 board game together and play a game in terminal.
		
		
## ConnectFourClient.java:
	To run: Java ConnectFourClient

### Options:
#### '-y' or '-Y': Rellow
	Request to be yellow player from args instead of through dialogs (if both red and yellow are selected, yellow takes priority).
		
#### '-r' or '-R': Red
	Request to be Red player from args instead of through dialogs (if both red and yellow are selected, yellow takes priority).
		
#### '-b' or '-B': Bot
	Request to pair with a computer-controlled player (bot) from args instead of through dialogs (if both human and bot are selected, bot will take priority).
		
#### '-h' or '-H': Human
	Request to pair with a human from args instead of through dialogs (if both human and bot are selected, bot will take priority).
		
#### '-s' or '-S': Simple
	Enable simple text mode, which allows older terminals to still play with ASCII-only terminals.
		
#### '-t' or '-T': Test
	Print a test file to determine if your terminal requires simple text mode.
			
### Description:
#### A client for 'MTServer.java' that just handles user I/O, just run the program after the server is running and listen to the instructions. Moves that you and the other player make are highlighted for legibility. When a victory is achieved all the winning pieces get highlighted.
