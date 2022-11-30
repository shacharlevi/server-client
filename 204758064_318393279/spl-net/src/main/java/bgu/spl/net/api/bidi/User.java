package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.Notification;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

public class User {
    private int id;
    private String name;
    private String password;
    private String birthday;
    private boolean isLoggedIn;
    private ConcurrentHashMap<String, User> i_am_following_after;
    private ConcurrentHashMap<String, User> my_followers;
    private LinkedList<String> blocked;
    private int age;
    private int numOfPosts;
    private ConcurrentLinkedQueue<Notification> unRead;

    public User(String name, String password, String birthday) {
        this.name = name;
        this.password = password;
        this.birthday = birthday;
        this.id = -1;
        isLoggedIn = false;
        i_am_following_after = new ConcurrentHashMap<>();
        my_followers = new ConcurrentHashMap<>();
        blocked = new LinkedList<>();
        age = (2022 - (parseShort(birthday.substring(6))));
        numOfPosts = 0;
        unRead= new ConcurrentLinkedQueue<>();
    }

    public void setUnRead(ConcurrentLinkedQueue<Notification> unRead) {
        this.unRead = unRead;
    }

    public ConcurrentLinkedQueue<Notification> getUnRead() {
        return unRead;
    }

    public void addUnRead(Notification notification) {
        unRead.add(notification);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public String getPassword() {
        return password;
    }


    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public synchronized void  login() {
        if(isLoggedIn){
            throw new IllegalStateException();
        }
        isLoggedIn = true;
    }

    public void logout() {
        isLoggedIn = false;
    }

    public void addFollowToSomeOneElse(User user) {
        i_am_following_after.put(user.getName(), user);
    }

    public void addNewFollowerToMe(User user) {
        my_followers.put(user.getName(), user);
    }

    public ConcurrentHashMap<String, User> getMy_followers() {
        return my_followers;
    }

    public ConcurrentHashMap<String, User> getI_am_following_after() {
        return i_am_following_after;
    }

    public void addBlocked(String name) {
        blocked.add(name);
    }

    public LinkedList<String> getBlocked() {
        return blocked;
    }

    public int getAge() {
        return age;
    }

    public void addNumOfPosts() {
        numOfPosts++;
    }

    public int getNumOfPosts() {
        return numOfPosts;
    }
}
