
package lk.sde.simplechatserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import lk.sde.common.ChatIF;
import lk.sde.common.ServerConsoleCommandFilter;

/**
 * This class deals with the user inputs to the server console.
 *
 * @author Yusuf
 */
public class ServerConsole implements ChatIF{


    // A reference to the EchoServer instance that this
    // console belongs to.
    EchoServer echoServer;

    public ServerConsole(){}

    public ServerConsole(EchoServer echoServer){
        this.echoServer = echoServer;
    }
    //Instance methods ************************************************

  /**
   * This method waits for input from the console.  Once it is
   * received, it sends it to the echo servers message handler.
   */
  public void accept()
  {
    try
    {
      BufferedReader fromConsole =
        new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true)
      {
        message = fromConsole.readLine();
        echoServer.handleMessageFromServerConsole(message);
        
      }
    }
    catch (Exception ex)
    {
      System.out.println
        ("Unexpected error while reading from console!");
    }
  }

  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message){
    System.out.println(message);
  }

}
