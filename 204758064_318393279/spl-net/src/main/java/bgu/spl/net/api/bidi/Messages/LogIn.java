package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.srv.ConnectionHandler;

public class LogIn implements BidiMessage {
    private String UserName;
    private String Password;
    private String Captcha;

    public LogIn(String userName, String password, String captcha) {
        UserName = userName;
        Password = password;
        Captcha = captcha;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getCaptcha() {
        return Captcha;
    }

    public void setCaptcha(String captcha) {
        Captcha = captcha;
    }

    @Override

    public Short getOPCode() {
        return 2;
    }

}