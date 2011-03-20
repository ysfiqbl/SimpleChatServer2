

package lk.sde.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Dinusha
 */
public class Login {
    protected Map<String, String> validUsersList;
    protected File usersFile;

    public 
    Login(String file)
    {
        usersFile = new File(file);
        validUsersList = new HashMap<String, String>();
    }

    public void 
    readUsersList()
    {
        StringTokenizer st = null;
        String line = "";
        // Does not check if the usersFile argument is a file or not
        if(usersFile.exists()){
            try{
                BufferedReader buff = new BufferedReader(
                        new FileReader(usersFile));
                    try{
                        while((line = buff.readLine()) != null){
                            st = new StringTokenizer(line, ",");

                            while(st.hasMoreTokens()){
                                validUsersList.put(st.nextToken(),
                                        st.nextToken());
                            }
                        }
                    }catch(IOException ex){}
                }catch(FileNotFoundException ex){}
        }else{
            try{
                usersFile.createNewFile();
            }catch(IOException ex){
                System.out.println("Unable to create new users file " +
                        ex.getMessage());
            }
        }
    }

    public boolean
    checkUsername(String username)
    {
        return validUsersList.containsKey(username);
    }

    protected void
    reloadUsersList()
    {
        if(!validUsersList.isEmpty())
        validUsersList.clear();

        this.readUsersList();
    }

    public void
    addNewUser(String username, String password)
    {
        if(!this.checkUsername(username)){
            // It is assumed that both username and password doesnot have a comma
            String line = username + "," + password;

            try{
                BufferedWriter buff = new BufferedWriter(
                        new FileWriter(usersFile, true));
                buff.write(line+"\n");
                buff.close();
            }catch(IOException ex){}

            this.reloadUsersList();
        }else{
            // Username exists, Send or print error message
        }
    }

    public boolean 
    isValidUser(String username, String password)
    {
        return (checkUsername(username) && (validUsersList.get(username)).
                toString().equals(password));
    }
}
