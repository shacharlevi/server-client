package bgu.spl.net.api.bidi;


import bgu.spl.net.api.bidi.Messages.*;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<BidiMessage> {
    private Connections<BidiMessage> connections;
    private int connectionId;
    private boolean terminate;
    private String name=null;


    @Override
    public void process(BidiMessage message) {
        //register
        if ( message.getOPCode() == 1) {
            //will return ack or error
            name= ((Register)message).getUserName();
            BidiMessage ans = DataBase.getInstance().register((Register) message,name);
            connections.send(connectionId, ans);
        }
        //login
        if (message.getOPCode() == 2) {
            name= ((LogIn)message).getUserName();
            BidiMessage ans = DataBase.getInstance().logIn((LogIn) message,connectionId);
            connections.send(connectionId, ans);
            if(ans.getOPCode()==10){
                for(Notification not: DataBase.getInstance().unReadM(name)){
                    connections.send(connectionId, not);
                }
            }

        }
        //logout
        if ( message.getOPCode() == 3) {
            BidiMessage ans = DataBase.getInstance().logOut((LogOut) message, name);
            if (ans.getOPCode()==10) {
                terminate = true;
            }
            connections.send(connectionId, ans);
        }
        //follow
        if (message.getOPCode() == 4) {
            BidiMessage ans = DataBase.getInstance().follow((Follow_UnFollow) message, name);
            connections.send(connectionId,  ans);
        }
        //post
        if (message.getOPCode() == 5) {
            BidiMessage ans = DataBase.getInstance().post((Post) message, name);
            connections.send(connectionId, ans);
        }
        //PM
        if (message.getOPCode() == 6) {
            BidiMessage ans = DataBase.getInstance().pm((PM) message, name);
            connections.send(connectionId,  ans);
        }
        //logStat
        if ( message.getOPCode() == 7) {
            DataBase.getInstance().logStat((LogStat) message, name,connectionId);
        }
        //stat
        if (message.getOPCode() == 8) {
            DataBase.getInstance().stat((Stat) message, name,connectionId);
        }
        //block
        if ( message.getOPCode() == 12) {
            BidiMessage ans = DataBase.getInstance().block((Block) message, name);
            connections.send(connectionId,  ans);
        }
    }

    @Override
    public void start(int connectionId, Connections<BidiMessage> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
        DataBase.getInstance().setConnections(connections);
        terminate = false;

    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
