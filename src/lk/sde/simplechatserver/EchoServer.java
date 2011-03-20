package lk.sde.simplechatserver;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lk.sde.common.Channel;
import java.io.IOException;
import lk.sde.common.ServerConsoleCommandFilter;
import lk.sde.common.ChatServerCommandFilter;
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
  ChatServerCommandFilter chatServerCommandFilter;
  List<Channel> channelList;

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
    chatServerCommandFilter = new ChatServerCommandFilter();
    channelList = new ArrayList<Channel>();
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
    public void handleMessageFromClient(Object msg, ConnectionToClient client){
        String[] parameterArray;
        String senderId = client.getInfo("loginId")!= null ?client.getInfo("loginId").toString() : null;

        if(msg.toString().startsWith("#login")){
            if(client.getInfo("loginId")==null){ // No previous login session
                client.setInfo("loginId", msg.toString().split("\\s")[1]);
                senderId = client.getInfo("loginId").toString();
                this.replyToClient(client, "Successfully logged in as "+senderId);
            }
            else{
                try {
                    client.sendToClient("SERVER MSG> #login should be the first command to be received. "
                                    + " You session is going to be terminated");
                    client.close();
                } catch (IOException ex) {}
            }
            return;
        }
        
        if(msg.toString().startsWith("#msg")){ // msg DestUser - Content
            String destUser=msg.toString().substring(1).split(" ")[1];
            int ContentIndex=msg.toString().indexOf("-");
            String destMsg = "Private Message From:"+client.getInfo("loginId").toString()+msg.toString().substring(ContentIndex);
            boolean isLoginUser = this.sendToClient(destUser, destMsg, client);
            if(!isLoginUser){
                try{
                    client.sendToClient("unable to send to the destination client");
                }catch (Exception ex) {
                    System.out.println("Error in replying Status");
                }
            }
            else{
                try{
                    client.sendToClient("send to the destination client successfully");
                }catch (Exception ex) {
                    System.out.println("Error in replying Status");
                }
            }
            return;
        }
        if(msg.toString().startsWith(ChatServerCommandFilter.COMMAND_SYMBOL+ChatServerCommandFilter.CHANNEL)){

            int contentIndex=-1;
            String destMsg = this.invalidChannelUsageMessage();
            StringBuilder channelListBuilder;
            List<String> channelNameList;
            try{
                parameterArray = msg.toString().split("\\s");
                if(ChatServerCommandFilter.CHANNEL_MSG.equals(parameterArray[1])){
                    // If this array has a length of less than 3 then the command is invalid
                    if(parameterArray.length<3){
                        this.sendToClient(senderId, destMsg, client);
                        return;
                    }

                    // If command is valid

                    // Send message to channel if user is in subsribed list
                    //#channel msg <user> -<message>
                    if((contentIndex = msg.toString().indexOf("-"))>0){
                        if(this.isChannel(parameterArray[2])){
                            if(this.isSubscribedToChannel(senderId, parameterArray[2])){
                                destMsg = "Message from channel "+parameterArray[2]+" by "+client.getInfo("loginId").toString()
                                            +msg.toString().substring(contentIndex);
                                this.sendToChannelList(this.getChannelSubscriberList(parameterArray[2]), destMsg, senderId, client);
                            }
                            else{
                               destMsg = "You are not subscribed to channel "+parameterArray[2];
                               this.replyToClient(client, destMsg);
                            }
                        }
                        else{
                            this.replyToClient(client, "Channel does not exist");
                        }
                    }                        
                 }
                 else if(ChatServerCommandFilter.CHANNEL_CREATE.equals(parameterArray[1])){
                        // #chanel create <channel_name>
                        if(!this.isChannel(parameterArray[2])){
                            this.createNewChannel(senderId, parameterArray[2]);
                            destMsg = "Successfully created channel "+parameterArray[2];
                        }
                        else{
                            destMsg = "Channel "+parameterArray[2]+" already exists";
                        }
                        this.replyToClient(client, destMsg);
                 }
                 else if(ChatServerCommandFilter.CHANNEL_REMOVE.equals(parameterArray[1])){
                        destMsg = this.removeChannel(senderId, parameterArray[2]);
                        this.replyToClient(client, destMsg);
                 }
                 else if(ChatServerCommandFilter.CHANNEL_SUBSCRIBE.equals(parameterArray[1])){
                        if(this.isChannel(parameterArray[2])){
                            if(!this.isSubscribedToChannel(senderId, parameterArray[2])){
                                this.subscribeToChannel(senderId, parameterArray[2]);
                                destMsg = "Successfully subscribed to channel "+parameterArray[2];
                            }
                            else{
                                destMsg = "Already subscribed to channel "+parameterArray[2];
                            }
                        }
                        else{
                            destMsg = "Channel "+parameterArray[2]+"does not exist";
                        }
                        this.replyToClient(client, destMsg);
                 }
                 else if(ChatServerCommandFilter.CHANNEL_UNSUBSCRIBE.equals(parameterArray[1])){
                        if(this.isSubscribedToChannel(senderId, parameterArray[2])){
                            if(!this.isChannelOwner(senderId, parameterArray[2])){
                                this.unsubscribeFromChannel(senderId, parameterArray[2]);
                                destMsg = "Successfully unsubscribed from channel "+parameterArray[2];
                            }
                            else{
                                destMsg = "You are the owner. You cannot unsubscribed from channel "+parameterArray[2];
                            }
                            
                        }
                        else{
                            destMsg = "Not subscribed to channel "+parameterArray[2];
                        }
                        this.replyToClient(client, destMsg);
                 }
                 else if(ChatServerCommandFilter.CHANNEL_LIST.equals(parameterArray[1])){
                        if(ChatServerCommandFilter.CHANNEL_LIST_ALL.equals(parameterArray[2])){
                            channelListBuilder = new StringBuilder();
                            channelNameList = this.getChannelNameList();
                            channelListBuilder.append("Channel List:\n");
                            for(String s:channelNameList){
                                channelListBuilder.append("-");
                                channelListBuilder.append(s);
                                channelListBuilder.append("\n");
                            }
                            destMsg = channelListBuilder.toString();
                                
                        }
                        else if(ChatServerCommandFilter.CHANNEL_LIST_SUBSCRIBED.equals(parameterArray[2])){
                            channelListBuilder = new StringBuilder();
                            channelNameList = this.getSubscribedList(senderId);
                            channelListBuilder.append("Subscribed Channel List:\n");
                            for(String s:channelNameList){
                                channelListBuilder.append("-");
                                channelListBuilder.append(s);
                                channelListBuilder.append("\n");
                            }
                            destMsg = channelListBuilder.toString();
                        }
                        
                        this.replyToClient(client, destMsg);

                  }
                  else{
                        // Invalid channel command
                       this.replyToClient(client, destMsg);
                  }
            }
            catch(ArrayIndexOutOfBoundsException ex){
                    this.replyToClient(client, destMsg);
            }
            return;
        }
        if(msg.toString().startsWith(ChatServerCommandFilter.COMMAND_SYMBOL+ChatServerCommandFilter.HELP)){
            this.replyToClient(client, "You can only send private messages or message to a channel.\nPrivate message command:\n-Send message to "
                    + "<user>:\n\t#msg <user> -<your message> \n\tE.g. #msg joe -How are you?\n"+getChannelUsage()+"\n"+getMonitorUsage());
            return;
        }

        if (msg.toString().startsWith("#forward")) {
            String[] tokens = msg.toString().split("#x");
            if (tokens.length < 3) {
                System.out.println("Invalid #forward command recieved. Ignoring.");
                return;
            }

            String destStr = tokens[1];
            String destMsg = tokens[2];

            destMsg = "Message received to " + client.getInfo("loginId").toString() + " forwarded. Message is: "+destMsg;
            System.out.println(destStr + " " + destMsg);
            String[] destinations = new String[] {};
            if (destStr.contains(",")) {
                destinations = destStr.split(",");
                for (int i = 0; i < destinations.length; i++) {
                    String destUser = destinations[i];
                    this.sendToClient(destUser, destMsg, client);
                }
                return;
            } else {
                this.sendToClient(destStr, destMsg, client);
                return;
            }
        }
        
        this.replyToClient(client, "You can only send private messages or message to a channel.\nPrivate message command:\n-Send message to "
                    + "<user>:\n\t#msg <user> -<your message> \n\tE.g. #msg joe -How are you?\n"+getChannelUsage()+"\n"+getMonitorUsage());
        
    }


    private String getMonitorUsage(){
       String channelUsage = "Monitor commands:\n"
               + "-Add a new monitor:\n\t #monitor add <userID>\n"
               + "-Remove a monitor:\n\t#monitor remove <userID>\n"
               + "-List all existing monitors:\n\t#monitor list\n"
               + "-Start monitoring:\n\t#monitor start\n"
               + "-Stop monitoring:\n\t#monitor stop\n";

       return channelUsage;
   }

    // Helper functions to Handle channels
    private String invalidChannelUsageMessage(){
        return "Invalid usage of the channel command.\n"+this.getChannelUsage();
    }
    private String getChannelUsage(){
        String channelUsage = "Channel commands:\n"
                + "-Create a new channel:\n\t #channel create <channel_name>\n"
                + "-Remove a channel created by you:\n\t#channel remove <channel_name>\n"
                + "-Subscribe to a channel:\n\t#channel subscribe <channel_name>\n"
                + "-Unsubscribe from  a channel:\n\t#channel unsubscribe <channel_name>\n"
                + "-List all existing channels:\n\t#channel list all \n"
                + "-List channels that you are subscribed to:\n\t#channel list subscribe\n"
                + "-Send a message to channel:\n\t #channel msg <channel_name> -<msg>\n";

        return channelUsage;
    }

    private boolean isChannel(String channelName){
        for(Channel c: channelList){
            if(c.getChannelName().equals(channelName)){
                return true;
            }
        }
        return false;
    }

    private Channel getChannel(String channelName){

        for(Channel c: channelList){
            if(c.getChannelName().equals(channelName)){
                return c;
            }
        }
        return null;
    }

    private void createNewChannel(String owner, String channelName){
        Channel c = new Channel(owner,channelName);
        c.getSubscriberList().add(owner);
        this.channelList.add(c);
    }

    private String removeChannel(String owner, String channelName){
        String returnMessage="Channel "+channelName+" does not exist";;
        Channel c;
        for(int i=0;i<channelList.size();i++){
            c = channelList.get(i);
            if(c.getChannelName().equals(channelName)){
                if(c.getOwner().equals(owner)){                            
                    channelList.remove(i);     
                    returnMessage = "Successfully removed channel "+c.getChannelName();
                }
                else{
                    returnMessage = "You are not the owner of channel "+channelName;
                }
            }
        }                
        return returnMessage;
    }
    
    private boolean isChannelOwner(String owner, String channelName){
        Channel c = this.getChannel(channelName);
        return c!=null && c.getOwner().equals(owner) ? true : false;
    }

    private boolean isSubscribedToChannel(String loginId, String channelName){
        List<String> subscriberList = this.getChannelSubscriberList(channelName);
        return (subscriberList.contains(loginId)) ?  true : false;
    }

    private void subscribeToChannel(String loginId, String channelName){
        Channel c;
        for(int i=0;i<channelList.size();i++){
            c = channelList.get(i);
            if(c.getChannelName().equals(channelName)){
                c.getSubscriberList().add(loginId);
            }
        }
    }

    private void unsubscribeFromChannel(String loginId, String channelName){
        Channel c;
        for(int i=0;i<channelList.size();i++){
            c = channelList.get(i);
            if(c.getChannelName().equals(channelName)){
                c.getSubscriberList().remove(loginId);
            }
        }
    }

    private List<String> getSubscribedList(String loginId){
        List<String> subscribedChannelList = new ArrayList<String>();
        for(Channel c: channelList){
            for(String user: c.getSubscriberList()){
                if(user.equals(loginId)){
                    subscribedChannelList.add(c.getChannelName());
                    break;
                }
            }
        }
        return subscribedChannelList;
    }

    private List<String> getChannelNameList(){
        List<String> channelNameList = new ArrayList<String>();
        for(Channel c: channelList){
            channelNameList.add(c.getChannelName());
        }
        return channelNameList;
    }

    private List<String> getChannelSubscriberList(String channelName){
        for(Channel c: channelList){
            if(c.getChannelName().equals(channelName)){
                return c.getSubscriberList();
            }
        }
        return new ArrayList();
    }

    private void sendToChannelList(List<String> subscriberList, String message, String sender, ConnectionToClient client){
        for(String subscriber:subscriberList){
            this.sendToClient(subscriber, message, client);
        }
        
    }

    private boolean sendToClient(String destUser, String msg, ConnectionToClient client){
        Thread[] clientThreadList =  this.getClientConnections();
        boolean isLoginUser=false;
        for (int i=0; i<clientThreadList.length; i++){
            try{
                String threadUser=((ConnectionToClient)clientThreadList[i]).getInfo("loginId").toString();
                if(threadUser.equalsIgnoreCase(destUser)){
                    ((ConnectionToClient)clientThreadList[i]).sendToClient(msg);
                    isLoginUser=true;
                    break;
                }
            }catch (Exception ex) {
                System.out.println("Error in sending Data");
            }
        }
        return isLoginUser;
    }

    private void replyToClient(ConnectionToClient client, String message){
        try {
            client.sendToClient(message);
        } catch (IOException ex) {
            System.out.println("Could not send message to client "+client.getInfo("loginId").toString());
        }
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
