package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Stat implements BidiMessage {
    private String[] usersName;


    public Stat(String[] usersName) {
        this.usersName = usersName;
    }


    @Override
    public Short getOPCode() {
        return 8;
    }

    public String[] getUsersName() {
        return usersName;
    }


}
