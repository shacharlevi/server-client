package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;
import bgu.spl.net.api.bidi.Messages.Error;
import bgu.spl.net.srv.ConnectionsImpl;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataBase {

    private static DataBase dataBase = new DataBase();
    private ConcurrentHashMap<String, User> users; //username,User
    private int counter = 0;
    private LinkedList<String> postAndPM;
    private Connections<BidiMessage> connections;
    private LinkedList<String> filteredWords;

    private DataBase() {
        users = new ConcurrentHashMap<>();
        postAndPM = new LinkedList<>();
        filteredWords = new LinkedList<>();
    }

    public Connections<BidiMessage> getConnections() {
        return connections;
    }

    public void setConnections(Connections<BidiMessage> connections) {
        this.connections = connections;
    }

    public static DataBase getInstance() {
        return dataBase;
    }

    public BidiMessage register(Register msg, String userName) {
        if (!users.containsKey(msg.getUserName())) {
            User user = new User(msg.getUserName(), msg.getPassword(), msg.getBirthday());
            if( users.putIfAbsent(msg.getUserName(), user)==null) {
                return new ACK((short) 1);
            }
            else{
                return new Error((short) 1);
            }
        } else
            return new Error((short) 1);
    }

    public BidiMessage logIn(LogIn msg,int connectionId) {
        if (!users.containsKey(msg.getUserName())) {
            return new Error((short) 2);
        }
        if (!(users.get(msg.getUserName()).getPassword().equals(msg.getPassword()))) {
            return new Error((short) 2);
        }
        if (users.get(msg.getUserName()).isLoggedIn()) {
            return new Error((short) 2);
        }
        if (msg.getCaptcha().equals("0")) {
            return new Error((short) 2);
        }
        User currUser = users.get(msg.getUserName());
        //incase of context switch
        try {
            currUser.login();
        }catch (Exception e){
            return new Error((short) 2);
        }
        currUser.setId(connectionId);
        return new ACK((short) 2);
    }
    public ConcurrentLinkedQueue<Notification> unReadM(String username){
        User currUser = users.get(username);
        ConcurrentLinkedQueue<Notification> newL = currUser.getUnRead();
        currUser.setUnRead(new ConcurrentLinkedQueue<>());
        return newL;
    }

    public BidiMessage logOut(LogOut msg, String name) {
        if (!users.containsKey(name)) {
            return new Error((short) 3);
        } else {
            if(!users.get(name).isLoggedIn()) {
                return new Error((short) 3);
            }
            else {
                users.get(name).logout();
                return new ACK((short) 3);
            }
        }
    }

    public BidiMessage follow(Follow_UnFollow msg, String name) {
        //error:if is not register, if the user we want to follow doesn't exist
        if ((!(users.containsKey(name))) || !(users.containsKey(msg.getUserName()))) {
            return new Error((short) 4);
        }
        User currUser = users.get(name);
        User otherUser = users.get(msg.getUserName());
        //error:if isnt log in
        if (!(currUser.isLoggedIn())) {
            return new Error((short) 4);
        }
        if (msg.getFollow_Unfollow().equals("0")) {//follow
            //error: already following,if blocked
            if (currUser.getI_am_following_after().containsKey(msg.getUserName()) || otherUser.getBlocked().contains(currUser.getName())) {
                return new Error((short) 4);
            } else {
                currUser.addFollowToSomeOneElse(otherUser);
                otherUser.addNewFollowerToMe(currUser);
                return new FollowACK(((short) 4), msg.getUserName());
            }
        } else {//unfollow
            if (!(currUser.getI_am_following_after().containsKey(otherUser.getName()))) {
                return new Error((short) 4);
            } else {
                currUser.getI_am_following_after().remove(otherUser.getName());
                otherUser.getMy_followers().remove(currUser.getName());
                return new FollowACK(((short) 4), msg.getUserName());
            }
        }
    }

    public BidiMessage post(Post msg, String name) {
        //if not register
        if (!(users.containsKey(name))) {
            return new Error((short) 5);
        }
        User currUser = users.get(name);
        //if not log in
        if (!(currUser.isLoggedIn())) {
            return new Error((short) 5);
        }
        postAndPM.add(msg.getContent());
        currUser.addNumOfPosts();
        Notification notification = new Notification((short) 1, currUser.getName(), msg.getContent());
        if(!currUser.getMy_followers().isEmpty()) {
            for (String userName : currUser.getMy_followers().keySet()) {
                if (users.get(userName).isLoggedIn()) {
                    connections.send((users.get(userName).getId()), notification);

                } else {
                    users.get(userName).addUnRead(notification);
                }
            }
        }
        if(!(msg.getUsersTagged().length==0)) {
            for (String userName : msg.getUsersTagged()) {
                if (users.containsKey(userName) && !(users.get(userName).getBlocked().contains(currUser.getName()))) {
                    if (users.get(userName).isLoggedIn()) {
                        if (!connections.send((users.get(userName).getId()), notification)) {
                            users.get(userName).addUnRead(notification);
                        }
                    } else {
                        users.get(userName).addUnRead(notification);
                    }
                }
            }
        }
        return new ACK((short) 5);
    }

    public BidiMessage pm(PM msg, String name) {
        //if not register
        if (!(users.containsKey(name))) {
            return new Error((short) 6);
        }
        User currUser = users.get(name);
        //if not log in
        if (!(currUser.isLoggedIn())) {
            return new Error((short) 6);
        }
        //if otheruser is Registered
        if (!(users.containsKey(msg.getUserName()))) {
            return new Error((short) 6);
        }
        User otherUser = users.get(msg.getUserName());
        //if curruser is blocked by OtherUser
        if (otherUser.getBlocked().contains(currUser.getName())) {
            return new Error((short) 6);
        }
        if (!(currUser.getI_am_following_after().containsKey(otherUser.getName()))) {
            return new Error((short) 6);
        }
        for (String s : filteredWords) {
            if (msg.getContent().contains(s)) {
                msg.setContent(msg.getContent().replaceAll(s, "<filtered>"));
            }
        }
        postAndPM.add(msg.getContent());
        Notification notification = new Notification((short) 0, currUser.getName(), msg.getContent());
        if (otherUser.isLoggedIn()) {
            if(!connections.send(otherUser.getId(), notification)){
                otherUser.addUnRead(notification);
            }
        }
        else {
            otherUser.addUnRead(notification);
        }
        return new ACK((short) 6);
    }

    public void logStat(LogStat msg, String name,int connectionId) {
        //if not register
        if (!(users.containsKey(name))) {
            connections.send(connectionId, new Error((short) 7));
        }
        User currUser = users.get(name);
        //if not log in
        if (!(currUser.isLoggedIn())) {
            connections.send(connectionId, new Error((short) 7));
        }
        for (User user : users.values()) {
            if (user.isLoggedIn() && user != currUser && (!(user.getBlocked().contains(currUser.getName())) || !(currUser.getBlocked().contains(user.getName())))) {
                connections.send(connectionId, new LogStatNStatAck((short) 7, user));
            }
        }
    }

    public void stat(Stat msg,String name ,int connectionId) {
        //if not register
        if (!(users.containsKey(name))) {
            connections.send(connectionId, new Error((short) 8));
        }
        User currUser = users.get(name);
        //if not log in
        if (!(currUser.isLoggedIn())) {
            connections.send(connectionId, new Error((short) 8));
        }
        boolean allRegistered=true;
        for (String userName : msg.getUsersName()) {
            if(!users.containsKey(userName)){
                allRegistered=false;
                break;
            }
        }
        if(allRegistered) {
            for (String userName : msg.getUsersName()) {
                if (!users.get(userName).getBlocked().isEmpty()) {
                    if (!(users.get(userName).getBlocked().contains(currUser.getName())) || !(currUser.getBlocked().contains(userName))) {
                        connections.send(connectionId, new Error((short) 8));
                    }
                }
                if (!(userName.equals(currUser.getName()))) {
                    connections.send(connectionId, new LogStatNStatAck((short) 8, users.get(userName)));
                }
            }
        }
        else{
            connections.send(connectionId, new Error((short) 8));
        }
    }

    public BidiMessage block(Block msg, String name) {
        if (!(users.containsKey(msg.getUserToBlockName()))) {
            return new Error((short) 12);
        }
        //if not register
        if (!(users.containsKey(name))) {
            return new Error((short) 12);
        }
        User currUser = users.get(name);
        //if not log in
        if (!(currUser.isLoggedIn())) {
            return new Error((short) 12);
        }
        User otherUser = users.get(msg.getUserToBlockName());
        currUser.addBlocked(msg.getUserToBlockName());
        if (currUser.getMy_followers().containsKey(msg.getUserToBlockName())) {
            currUser.getMy_followers().remove(msg.getUserToBlockName());
        }
        if (currUser.getI_am_following_after().containsKey(msg.getUserToBlockName())) {
            currUser.getI_am_following_after().remove(msg.getUserToBlockName());
        }
        if (otherUser.getMy_followers().containsKey(name)) {
            otherUser.getMy_followers().remove(currUser.getName());
        }
        if (otherUser.getI_am_following_after().containsKey(name)) {
            otherUser.getI_am_following_after().remove(currUser.getName());
        }
        return new ACK((short) 12);
    }
}