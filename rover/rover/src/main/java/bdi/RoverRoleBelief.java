package bdi;

/**
 * Created by Violet on 04/11/2016.
 */
public class RoverRoleBelief extends Belief {

    String role;
    String clientID;

    public RoverRoleBelief(String clientID, String role) {
        this.clientID = clientID;
        this.role = role;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


}
