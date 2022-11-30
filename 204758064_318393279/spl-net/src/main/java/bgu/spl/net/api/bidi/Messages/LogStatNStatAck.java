package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.User;

public class LogStatNStatAck extends ACK {
    private int age;
    private int numOfPosts;
    private int numOfFollowers;
    private int numOfFollowing;

    public LogStatNStatAck(short msgOpCode, User user) {
        super(msgOpCode);
        age = user.getAge();
        numOfPosts = user.getNumOfPosts();
        numOfFollowers = user.getMy_followers().size();
        numOfFollowing = user.getI_am_following_after().size();
    }


    public int getAge() {
        return age;
    }


    public int getNumOfPosts() {
        return numOfPosts;
    }

    public int getNumOfFollowers() {
        return numOfFollowers;
    }


    public int getNumOfFollowing() {
        return numOfFollowing;
    }


}
