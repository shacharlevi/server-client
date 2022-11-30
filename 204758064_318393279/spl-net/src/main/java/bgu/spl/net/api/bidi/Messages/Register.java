package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class Register implements BidiMessage {
    private String userName;
    private String password;
    private String birthday;


    public Register(String userName, String password, String birthday) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;

    }


    @Override
    public Short getOPCode() {
        return 1;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

}
