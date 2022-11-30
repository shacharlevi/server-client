package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;

public class TCPMain {
    public static void main(String[] args) {
        int port =Integer.parseInt(args[0]);
        Server.threadPerClient(port, BidiMessagingProtocolImpl::new, BidiMessageEncoderDecoder::new).serve();
       // Server.threadPerClient(7777, BidiMessagingProtocolImpl::new, BidiMessageEncoderDecoder::new).serve();

    }

}

