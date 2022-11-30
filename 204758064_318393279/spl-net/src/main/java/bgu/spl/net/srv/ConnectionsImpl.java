package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.DataBase;
import bgu.spl.net.api.bidi.User;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<ConnectionHandler<T>, Queue<? extends BidiMessage>> messages;
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> mapIdCon;
    private int counter;


    public ConnectionsImpl() {
        counter = 0;
        mapIdCon = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
    }


    public boolean send(int connectionId, T msg) {
        if (mapIdCon.containsKey(connectionId)) {
            mapIdCon.get(connectionId).send(msg);
            return true;
        } else {
            return false;
        }
    }


    public void broadcast(T msg) {
        for (ConnectionHandler ch : mapIdCon.values()) {
            mapIdCon.get(ch).send(msg);
        }
    }

    public void disconnect(int connectionId) {
        mapIdCon.remove(connectionId);
    }

    public void addCustomer(ConnectionHandler<T> connectionHandler) {
        mapIdCon.put(counter, connectionHandler);
        counter++;
    }

    public Queue<? extends BidiMessage> getMessages(ConnectionHandler<T> connectionHandler) {
        Queue<? extends BidiMessage> q = messages.get(connectionHandler);
        messages.put(connectionHandler, new ConcurrentLinkedQueue<>());
        return q;
    }

    public int getconnid(ConnectionHandler ConnectionHandler) {
        for (Integer id : mapIdCon.keySet()) {
            if (mapIdCon.get(id).equals(ConnectionHandler)) {
                return id;
            }
        }
        return -1;
    }

}
