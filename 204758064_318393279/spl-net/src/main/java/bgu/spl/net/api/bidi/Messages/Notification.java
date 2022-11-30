package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Notification implements BidiMessage {
    private short type;
    private String postingUser;
    private String content;



    public Notification(short type, String postingUser, String content) {
        this.type = type;
        this.postingUser = postingUser;
        this.content = content;


    }



    public String getPostingUser() {
        return postingUser;
    }

    public void setPostingUser(String postingUser) {
        this.postingUser = postingUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Short getOPCode() {
        return 9;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }


}
