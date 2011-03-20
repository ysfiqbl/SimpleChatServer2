
package lk.sde.common;

/**
 * The <code>ServerConsoleCommandFilter</code> class handles
 * has the definitions and the verification methods to deal with
 * commands entered to the server console. That is any input beginning
 * with '#'
 *
 * @author Yusuf
 */
public class ServerConsoleCommandFilter {
    /**
     * To recognize an input as a command, the input needs to be
     * prefixed by the COMMAND_SYMBOL. Here it is '#'
     *
     */
    public final static String COMMAND_SYMBOL = "#";

    /**
     * Any input to the server console will have the
     * SERVER_MSG_PREFIX as its prefix when it is echoed back to
     * the console.
     */
    public final static String SERVER_MSG_PREFIX = "SERVER MSG>";

    /**
     * List of all the commands that the server can perform.
     */
    public final static String QUIT = "quit";
    public final static String STOP = "stop";
    public final static String CLOSE = "close";
    public final static String SET_PORT = "setport";
    public final static String START = "start";
    public final static String GET_PORT = "getport";

    // Channel commands
    public final static String CHANNEL = "channel";
    public final static String CHANNEL_CREATE = "create";
    public final static String CHANNEL_LIST_ALL = "list all";
    public final static String CHANNEL_SUBSCRIBE = "subscribe";
    public final static String CHANNEL_UNSUBSCRIBE = "unsubscribe";
    public final static String CHANNEL_LIST_SUBSCRIBED = "list subscribed";

    // Private message commands
    public final static String MSG = "msg";

    /**
     * Determine whether an input is a command or not.
     *
     * @param command
     * @return
     */
    public boolean isCommand(String command){
        return command.startsWith(COMMAND_SYMBOL) ? true : false;
    }

    /**
     * Remove the COMMAND_SYMBOL from the input, lower case it
     * and remove both trailing and leading whitespace characters.
     * return the resulting String.
     *
     * @param command
     * @return
     */
    public String getCommand(String command){
        return command.trim().substring(1,command.length()).toLowerCase();
    }


    /**
     * Validates a user entered port number. 
     *
     * Returns -1 if no parameter was give.
     * Returns -2 if the parameter is not a numeric
     * Else returns the integer representation of the port number.
     *
     * @param inputArray
     * @return
     */
    public int getSetPortParameter(String[] inputArray){
        int port = 0;

        try{
            port = Integer.parseInt(inputArray[1]);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            port = -1;
        }
        catch(NumberFormatException ex){
            port = -2;
        }
        finally{
            return port;
        }
    }

}
