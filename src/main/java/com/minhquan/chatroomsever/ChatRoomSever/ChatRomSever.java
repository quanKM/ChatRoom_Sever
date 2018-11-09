/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.minhquan.chatroomsever.ChatRoomSever;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import Model.User;
import Model.Message;
import Model.MessageType;
import java.nio.ByteBuffer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Minh Quan
 */
public class ChatRomSever extends WebSocketServer {

    private final static Logger logger = LogManager.getLogger(ChatRomSever.class);

    private HashMap<WebSocket, User> users;

    private Set<WebSocket> conns;

    private ChatRomSever(int port) {
        super(new InetSocketAddress(port));
        conns = new HashSet<>();
        users = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

        conns.add(webSocket);

        logger.info("Connection established from: " + webSocket.getRemoteSocketAddress().getHostString());
        System.out.println("New connection from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        // When connection is closed, remove the user.
        try {
            removeUser(conn);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        logger.info("Connection closed to: " + conn.getRemoteSocketAddress().getHostString());
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            System.out.println(message);
            Message msg = mapper.readValue(message, Message.class);

            switch (msg.getType()) {
                case USER_JOINED:
                    addUser(new User(msg.getUser().getName()), conn);
                    break;
                case USER_LEFT:
                    removeUser(conn);
                    break;
                case TEXT_MESSAGE:
                    broadcastMessage(msg);
                case IMAGE_MESSAGE:
                    break;
            }

            System.out.println("Message from user: " + msg.getUser() + ", text: " + msg.getData() + ", type:" + msg.getType());
            logger.info("Message from user: " + msg.getUser() + ", text: " + msg.getData());
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

        if (conn != null) {
            conns.remove(conn);
        }
        assert conn != null;
        System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    private void broadcastMessage(Message msg) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String messageJson = mapper.writeValueAsString(msg);
            for (WebSocket sock : conns) {
                sock.send(messageJson);
            }
        } catch (JsonProcessingException e) {
            logger.info("Cannot convert message to json.");
        }
    }

    private void addUser(User user, WebSocket conn) throws JsonProcessingException {
        users.put(conn, user);
        acknowledgeUserJoined(user, conn);
        broadcastUserActivityMessage(MessageType.USER_JOINED);
    }

    private void removeUser(WebSocket conn) throws JsonProcessingException {
        users.remove(conn);
        broadcastUserActivityMessage(MessageType.USER_LEFT);
    }

    private void acknowledgeUserJoined(User user, WebSocket conn) throws JsonProcessingException {
        Message message = new Message();
        message.setType(MessageType.USER_JOINED_ACK);
        message.setUser(user);
        conn.send(new ObjectMapper().writeValueAsString(message));
    }

    private void broadcastUserActivityMessage(MessageType messageType) throws JsonProcessingException {

        Message newMessage = new Message();

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(users.values());
        newMessage.setData(data);
        newMessage.setType(messageType);
        broadcastMessage(newMessage);
    }

    public static void main(String[] args) {

        int port;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
            System.out.println("Run");
        } catch (NumberFormatException nfe) {
            port = 9000;
        }
        new ChatRomSever(port).start();
    }
}
