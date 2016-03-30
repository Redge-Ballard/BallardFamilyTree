package azulius.ballardfamilytree.model;

/**
 * Created by Azulius on 3/17/16.
 */
public class LoginResult {

    private String authorization;
    private String userName;
    private String personId;

    public LoginResult (String authorization, String userName, String personId) {
        this.authorization = authorization;
        this.userName = userName;
        this.personId = personId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getUserName() {
        return userName;
    }

    public String getPersonId() {
        return personId;
    }
}
