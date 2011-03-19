package lk.sde.simplechatserver;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lk.sde.common.ServerConsoleCommandFilter;
import java.net.InetAddress;
import lk.sde.ocsf.server.AbstractServer;
import lk.sde.ocsf.server.ConnectionToClient;
import static lk.sde.common.ServerConsoleCommandFilter.*;

// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 



/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;

  ServerConsole serverConsole;
  ServerConsoleCommandFilter serverConsoleCommandFilter;
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
    serverConsoleCommandFilter = new ServerConsoleCommandFilter();
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient(Object msg, ConnectionToClient client)
  {
      if(msg.toString().startsWith("#login")){
          if(client.getInfo("loginId")==null){ // No previous login session
            client.setInfo("loginId", msg.toString().split("\\s")[1]);
          }
          else{
            try {
                client.sendToClient("SERVER MSG> #login should be the first command to be received. "
                            + " You session is going to be terminated");
                client.close();
            } catch (IOException ex) {}
          }
      }
      
      System.out.println("Message received: " +client.getInfo("loginId")+"> "+ msg + " from " + client);
      this.sendToAllClients(msg);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client Connected: Establised connection with " + client +
                ". Total connections to server = " + this.getNumberOfClients());
        client.setInfo("inetAddress", client.getInetAddress());
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client Disconnected: " + client.getInfo("loginId")+"@"+client.getInfo("inetAddress")
                + " disconnected from server.");
    }

    @Override
    protected synchronized void clientException(ConnectionToClient client, Throwable exception) {        
        System.out.println("Client Disconnected: " + client.getInfo("loginId")+"@"+client.getInfo("inetAddress")
                + " disconnected from server.");
    }

    @Override
    protected void listeningException(Throwable exception) {
        System.out.println("Listening exception");
        exception.printStackTrace();
    }




  //Class methods ***************************************************
    public ServerConsole getServerConsole() {
        return serverConsole;
    }

    public void setServerConsole(ServerConsole serverConsole) {
        this.serverConsole = serverConsole;
    }

    /**
     * Handle inputs received from the Server Console.
     *
     * @param message
     */
    public void handleMessageFromServerConsole(String message){
        String command="";
        try
        {
            if(serverConsoleCommandFilter.isCommand(message)){
                command = serverConsoleCommandFilter.getCommand(message);
                processCommand(command);
            }
            else{
                sendToAllClients(ServerConsoleCommandFilter.SERVER_MSG_PREFIX + message);
                serverConsole.display(ServerConsoleCommandFilter.SERVER_MSG_PREFIX + message);
            }
        }
        catch(IOException e)
        {
            if(START.equals(command)){
                serverConsole.display("Cannot contact the server. Please try again later.");
            }
            else{
                serverConsole.display("You need to be logged on to send a message to the server. Use the #login command to connect to the server.");
            }
        }
    }


    /**
     * This method handles the commands to the server console.
     * That is anything beginning with '#'
     *
     * @param input
     * @throws IOException
     */
    private void processCommand(String input) throws IOException{
        String[] inputArray = input.split("\\s");
        String command = inputArray[0];

        if(START.equals(command)){
            if(!this.isListening()) this.listen();
            else serverConsole.display("Server is already listening for connections.");
            return;
        }

        if(STOP.equals(command)){
            if(isListening()) stopListening();
            else serverConsole.display("Already stopped listening.");
            return;
        }

        if(CLOSE.equals(command)){
            close();
            return;
        }

        if(GET_PORT.equals(command)){
            serverConsole.display("PORT: "+this.getPort());
            return;
        }

        if(SET_PORT.equals(command)){
            int parameter = serverConsoleCommandFilter.getSetPortParameter(inputArray);

            if(parameter==-1){
                serverConsole.display("Please specify the port. Usage:#setport <port>");
            }
            else if(parameter==-2){
                serverConsole.display("Port should be a numeric value.");
            }
            else{
                if(!isListening()){
                    this.setPort(parameter);
                }
                else{
                    serverConsole.display("You stop listening in order to set the port.");
                }
            }

            return;
        }

        if(QUIT.equals(command)){
            
            //isQuit=true;
            this.close();
            serverConsole.display("Exiting server.");
            System.exit(0);
            return;
        }

        serverConsole.display("#" + command +" is not a valid command");
    }
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    sv.setServerConsole(new ServerConsole(sv));
    
    try 
    {
      //sv.listen(); //Start listening for connections
       sv.getServerConsole().display("Server started. Execute the #start command to listen to connections.");
      sv.getServerConsole().accept();
      
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
