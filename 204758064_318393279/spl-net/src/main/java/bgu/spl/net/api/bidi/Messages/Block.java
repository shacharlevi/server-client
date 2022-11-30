package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Block implements BidiMessage {
    private String userToBlockName;


    public Block(String userToBlockName) {
        this.userToBlockName = userToBlockName;


    }


    @Override
    public Short getOPCode() {
        return 12;
    }

    public String getUserToBlockName() {
        return userToBlockName;
    }
}
