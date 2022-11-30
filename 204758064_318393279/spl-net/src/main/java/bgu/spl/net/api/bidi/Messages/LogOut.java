package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class LogOut implements BidiMessage {


    @Override
    public Short getOPCode() {
        return 3;
    }


}
