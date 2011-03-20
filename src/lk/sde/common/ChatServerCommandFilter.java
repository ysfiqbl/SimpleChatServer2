/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lk.sde.common;

/**
 * The <code>ChatServerCommandFilter</code> class handles
 * has the definitions and the verification methods to deal with
 * commands received by a client. That is any input beginning
 * with '#'
 *
 * @author Yusuf
 */
public class ChatServerCommandFilter {

    /**
     * To recognize an input as a command, the input needs to be
     * prefixed by the COMMAND_SYMBOL. Here it is '#'
     *
     */
    public final static String COMMAND_SYMBOL = "#";

    /**
     * List of all the commands that the server can perform.
     */
    public final static String CHANNEL = "channel";
    public final static String CHANNEL_MSG = "msg";
    public final static String CHANNEL_CREATE = "create";
    public final static String CHANNEL_REMOVE = "remove";
    public final static String CHANNEL_LIST = "list";
    public final static String CHANNEL_LIST_ALL = "all";
    public final static String CHANNEL_SUBSCRIBE = "subscribe";
    public final static String CHANNEL_UNSUBSCRIBE = "unsubscribe";
    public final static String CHANNEL_LIST_SUBSCRIBED = "subscribed";
    public final static String MSG = "msg";
    public final static String HELP = "help";



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

}
