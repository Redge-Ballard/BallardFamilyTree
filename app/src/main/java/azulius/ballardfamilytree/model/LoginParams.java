package azulius.ballardfamilytree.model;

/**
 * Created by Azulius on 3/17/16.
 */
public class LoginParams {

    private String username;
    private String password;
    private String serverName;
    private String serverPort;

    public LoginParams(String user, String pass, String name, String port){
        this.username = user;
        this.password = pass;
        this.serverName = name;
        this.serverPort = port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerPort() {
        return serverPort;
    }
}
