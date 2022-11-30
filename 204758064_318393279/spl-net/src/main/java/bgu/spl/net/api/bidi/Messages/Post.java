package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Post implements BidiMessage {
    private String content;
    private String[] usersTagged;


    public Post(String content,String[] usersTagged) {
        this.content = content;

        this.usersTagged=usersTagged;
    }

    @Override
    public Short getOPCode() {
        return 5;
    }

    public String getContent() {
        return content;
    }

    public String[] getUsersTagged() {
        return usersTagged;
    }
}
