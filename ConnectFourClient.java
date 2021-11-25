import java.io.*;
import java.net.*;
import java.util.Scanner;

// @author Daniel Nucci
// Assignment #1 for Dr. Q SOFE 4790
// Multi-threaded Connect Four Server (MTServer.java) / Client (ConnectFourClient.java)

public class ConnectFourClient {
    public static void main(final String args[]) throws Exception {
        final int SERVER_PORT_NUMBER = 3500;
        Socket socket;
	    Scanner scan = new Scanner(System.in);
        BufferedReader bReader;
        DataOutputStream outStream;
        char playerColour;

        System.out.println("\033[H\033[2J" + "Welcome to Connect 4!"); // clear screen and print welcome message

        // args from the command line and handling for said args
        boolean yArg = false;
        boolean rArg = false;
		boolean bArg = false;
        boolean hArg = false;
        boolean sArg = false;
        boolean tArg = false;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-y") && !yArg && !rArg) {
					System.out.println("You have requested to be yellow.");
					yArg = true;
                } 
                else if (args[i].equalsIgnoreCase("-r") && !rArg && !yArg) {
					System.out.println("You have requested to be red.");
					rArg = true;
                } 
                else if (args[i].equalsIgnoreCase("-b") && !bArg && !hArg) {
					System.out.println("You have requested a match with a bot.");
					bArg = true;
                }
                else if (args[i].equalsIgnoreCase("-h") && !hArg && !hArg) {
					System.out.println("You have requested a match with a human.");
					hArg = true;
                }
                else if (args[i].equalsIgnoreCase("-s") && !sArg) {
					System.out.println("You have requested simple text.");
					sArg = true;
                }
                else if (args[i].equalsIgnoreCase("-t") && !tArg) {
                    System.out.println("Here is a test ANSI circle to determine if you need simple text: ●");
                    return;
                }
			}
		}
        
        //Get player colour choice early on to handle matchmaking
        while (true) { 
            if (!rArg && !yArg) {
                System.out.println("Which colour do you with to play? ([R]ed / [Y]ellow)");
                String redOrYellow = scan.nextLine().toLowerCase();
                if (redOrYellow.equals("r") || redOrYellow.equals("y")) {
                    playerColour = redOrYellow.charAt(0);
                }
                else if (redOrYellow.equals("red")) {
                    playerColour = 'r';
                }
                else if (redOrYellow.equals("yellow")) {
                    playerColour = 'y';
                }
                else {
                    continue;
                }
            }
            else if (rArg) {
                playerColour = 'r';
            }
            else if (yArg) {
                playerColour = 'y';
            }
            else {
                playerColour = 'y';
            }
            break;
        }

        // try to setup socket after player colour is chosen
        try {
            socket = new Socket("localhost", SERVER_PORT_NUMBER);
            bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("There are no available connections at that socket. Please try again later.");
            scan.close();
            return;
        }

        // thread number checking logic (client side)
	    try {
	 	    if (bReader.readLine().equals("Busy!")) { //(this readline binds if there is no server message)
                System.out.println("Busy!"); //'busy' branch
                socket.close();
		    }
            else { //'available' branch
                //Hand the player colour off to the server
                outStream.writeChar(playerColour);
                outStream.flush();
                if (sArg) {
                    outStream.writeChars("true\n");
                }
                else {
                    outStream.writeChars("false\n");
                }
                outStream.flush();
                System.out.println("Connected to server.");

                if (bReader.readLine().equalsIgnoreCase("nomatch")) { //no open matches
                    System.out.println("It looks like there are no matches open for you.");

                    // pair with a bot or human
                    while (true) {
                        if (!bArg && !hArg) {
                            System.out.println("Would you like to wait for a match or pair with a bot? ([W]ait, [Bot])");
                            String waitOrBot = scan.nextLine().toLowerCase();

                            if (waitOrBot.equals("w") || waitOrBot.equals("b")) {
                                outStream.writeChar((int) waitOrBot.charAt(0));
                            }
                            else if (waitOrBot.equals("wait")) {
                                outStream.writeChar((int)'w');
                            }
                            else if (waitOrBot.equals("bot")) {
                                outStream.writeChar((int)'b');
                            }
                            else {
                                continue;
                            }
                        }
                        else if (hArg) {
                            outStream.writeChar((int)'w');
                        }
                        else if (bArg) {
                            outStream.writeChar((int)'b');
                        }
                        outStream.flush();
                        break;
                    }
                    System.out.println("Please wait for a match...");
                }
                System.out.println("You are now connected to " + bReader.readLine());

                System.out.println("Please press enter when ready...");
                scan.nextLine();
                
                System.out.print("\033[H\033[2J"); // clear screen

                outStream.writeBytes("start\n");

                // start gameplay
                while (true) {
                    if (bReader.readLine().equalsIgnoreCase("start_turn")) {
                        if (bReader.readLine().equals("wait")) { //opponent's turn
                            System.out.println("The opponent is moving please wait...");
                            outStream.writeBytes("-1\n");
                        }
                        else { //current player's turn
                            System.out.print("\033[H\033[2J"); // clear screen
                            System.out.println("Your colour is: " + (playerColour == 'y' ? "Yellow." : "Red.")); // display player colour to user

                            bReader.read(); // read the writeUTF header
                            bReader.read(); //same deal down here
                            String gameRender = bReader.readLine();
                            String[] splitRender = gameRender.split(";");

                            gameRender = "";
                            for (int i = 0; i < splitRender.length; i++) {
                                gameRender += splitRender[i] + "\n";
                            }

                            System.out.print(gameRender); // print gameboard to user to see the state of the baord
                            System.out.println(" 1  2  3  4  5  6  7 ");

                            // collect user move
                            String move = "";
                            boolean isMoveValid = false;
                            int moveNum = 0;
                            do {
                                System.out.println("Please input a move (1 - 7)...");
                                move = scan.nextLine();
                                moveNum = 0;
                                try {
                                    moveNum = Integer.parseInt(move);
                                     
                                } catch (Exception e) {
                                    isMoveValid = false;
                                }
                                isMoveValid = (moveNum <= 7 && moveNum >= 1);
                            } while (!isMoveValid);
                            outStream.writeBytes(moveNum + "\n");
                        }
                        outStream.flush();

                        if (bReader.readLine().equalsIgnoreCase("end")) {}
                    } 
                    else {
                        System.out.print("\033[H\033[2J"); // clear screen
                        System.out.println("Your colour is: " + (playerColour == 'y' ? "Yellow." : "Red.")); // display player colour to user

                        bReader.read(); // read the writeUTF header
                        bReader.read(); // same deal down here
                        String gameRender = bReader.readLine();
                        String[] splitRender = gameRender.split(";");

                        gameRender = "";
                        for (int i = 0; i < splitRender.length; i++) {
                            gameRender += splitRender[i] + "\n";
                        }

                        System.out.print(gameRender);
                        int gameWinner = Integer.parseInt(bReader.readLine());
                        if (gameWinner == 1 && playerColour == 'y' || gameWinner == -1 && playerColour == 'r') {
                            System.out.println("You won the match, we hope you play again");
                        }
                        else if (gameWinner == 1 && playerColour == 'r' || gameWinner == -1 && playerColour == 'y') {
                            System.out.println("You lost the match, we hope you play again");
                        }
                        else {
                            System.out.println("The board is full with no winners: cat's game");
                        }
                        
                        break;
                    }
                }
            }
        }
        catch(IOException e) {
            System.out.println("Busy!");
        }
    socket.close();
    scan.close();
    }
}
