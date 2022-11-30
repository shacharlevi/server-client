package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Error implements BidiMessage {
    private short OP;


    public Error(short OP) {

        this.OP = OP;
    }

    public short getMOP() {
        return OP;
    }

    @Override
    public Short getOPCode() {
        return 11;
    }

}
