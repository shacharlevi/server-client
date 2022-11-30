package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Follow_UnFollow implements BidiMessage {
    private String Follow_Unfollow;
    private String UserName;

    public Follow_UnFollow(String follow_Unfollow, String userName) {
        Follow_Unfollow = follow_Unfollow;
        UserName = userName;

    }


    @Override
    public Short getOPCode() {
        return 4;
    }

    public String getFollow_Unfollow() {
        return Follow_Unfollow;
    }

    public String getUserName() {
        return UserName;
    }

}