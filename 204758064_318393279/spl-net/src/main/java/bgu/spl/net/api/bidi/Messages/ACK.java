package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class ACK implements BidiMessage {
    private short msgOpCode;


    public ACK(short msgOpCode) {
        this.msgOpCode = msgOpCode;

    }

    @Override
    public Short getOPCode() {
        return 10;
    }


    public short getMsgOpCode() {
        return msgOpCode;
    }
}
