package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class PM implements BidiMessage {
    private String UserName;
    private String content;
    private String dateNtime;


    public PM(String userName, String content, String dateNtime) {
        UserName = userName;
        this.content = content;
        this.dateNtime = dateNtime;

    }

    @Override
    public Short getOPCode() {
        return 6;
    }

    public String getUserName() {
        return UserName;
    }

    public String getContent() {
        return content;
    }

    public String getDateNtime() {
        return dateNtime;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
