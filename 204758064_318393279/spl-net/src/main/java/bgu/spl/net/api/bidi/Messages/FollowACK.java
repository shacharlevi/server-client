package bgu.spl.net.api.bidi.Messages;

public class FollowACK extends ACK {
    private short followOpCode;
    private String userName;

    public FollowACK(short msgOpCode, String userName) {
        super(msgOpCode);
        this.userName = userName;
        followOpCode = 4;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
