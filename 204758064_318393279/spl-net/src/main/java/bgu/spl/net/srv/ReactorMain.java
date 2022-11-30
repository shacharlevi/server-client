package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;

public class ReactorMain {
    public static void main(String[] args) {
        int port =Integer.parseInt(args[0]);
        int numoft =Integer.parseInt(args[1]);
        Server.reactor(numoft,port, BidiMessagingProtocolImpl::new, BidiMessageEncoderDecoder::new).serve();
    }
}
