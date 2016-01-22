package com.mkdika.bpchat.ui.servlet;

import com.mkdika.bpchat.entity.ChatMessage;
import com.mkdika.bpchat.ui.servlet.util.ChatMessageDecoder;
import com.mkdika.bpchat.ui.servlet.util.ChatMessageEncoder;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maikel Chandika <mkdika@gmail.com>
 */
@ServerEndpoint(value = "/ws/{room}", encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class)
public class WebSocketServlet {

    private final Logger log = LoggerFactory.getLogger(WebSocketServlet.class);

    private static Map<Session, String> nickNames = new ConcurrentHashMap<>();

    private Session currentSession;

    @OnOpen
    public void open(final Session session, @PathParam("room") final String room) {
        log.info("session openend and bound to room: " + room);

        Map<String, List<String>> map = session.getRequestParameterMap();

        if (map.containsKey("user")) {
            List<String> list = map.get("user");
            session.getUserProperties().put("user", list.get(0));

            if (nickNames.containsValue(session.getUserProperties().put("user", list.get(0)))) {
                Set<String> userList = new LinkedHashSet<>();
                userList.add((String) session.getUserProperties().put("user", list.get(0)));

                ChatMessage msg = new ChatMessage();
                msg.setMessage("user_name_already_in_use");
                msg.setNicknames(userList);
                msg.setReceived(new Date());
                msg.setSender((String) session.getUserProperties().put("user", list.get(0)));
                // send message. Must be evaluated in client.
                session.getAsyncRemote().sendObject(msg);
                return;
            } else {
                String str = map.get("user").toString();
                // remove '[]' from string
                str = str.substring(1, str.length() - 1);
                nickNames.put(session, str);
            }
        }

        // add room name to connected users properties.
        session.getUserProperties().put("room", room);

        this.currentSession = session;
        sendGreetings(session);
    }

    @OnClose
    public void close(Session session) {

        // clean users
        nickNames.remove(session);

        /**
         * Notify other users for leaving the room.
         */
        String room = "";
        String user = "";
        Map<String, Object> map = session.getUserProperties();
        if (map.containsKey("room")) {
            room = (String) session.getUserProperties().get("room");
        }
        if (map.containsKey("user")) {
            user = (String) session.getUserProperties().get("user");
        }
        String sender = user;
        String msg = "<b>" + sender + "</b>" + " has left the chat";
        java.util.Date received = new java.util.Date();

        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setMessage(msg);
        chatMsg.setSender(sender);
        chatMsg.setReceived(received);

        // add the users nicknames to the list
        Set<String> userList = new LinkedHashSet<>();
        for (String str : nickNames.values()) {
            userList.add(str);
        }
        chatMsg.setNicknames(userList);

        /**
         * If user close the browser. <br>
         * Chrome: onError<br>
         * Firefox: onClose<br>
         */
        onMessage(session, chatMsg);
    }

    @OnError
    public void onError(Throwable error) {
        /**
         * Chrome Browser. If users closes the browser without correct
         * disconnecting.
         */
        log.error("WebsocketServlet Error: " + error);
    }

    @OnMessage
    public void onMessage(final Session session, final ChatMessage chatMessage) {

        /**
         * Send the message to all connected users in the same room.
         */
        String room = (String) session.getUserProperties().get("room");
        System.out.println(">>> Room: " + room);

        try {
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen() && (room != null) && room.equals(s.getUserProperties().get("room"))) {
                    System.out.println(">>>>>> user: " + s.getUserProperties().get("user") + ", room: " + s.getUserProperties().get("room"));

                    /**
                     * Collect the users nicknames which are connected to this
                     * room.
                     */
                    Set<String> userList = new LinkedHashSet<>();

                    for (Session ses : nickNames.keySet()) {
                        String userRoom = (String) ses.getUserProperties().get("room");
                        String notifyRoom = (String) session.getUserProperties().get("room");

                        if (userRoom.equals(notifyRoom)) {
                            userList.add(nickNames.get(ses));
                        }
                    }

                    chatMessage.setNicknames(userList);

                    // Check if user has closed the browser
                    try {
                        s.getBasicRemote().sendObject(chatMessage);
                    } catch (IllegalStateException e) {
                        log.error(e.toString());
                    }
                }
            }
        } catch (IOException | EncodeException e) {
            log.error(e.toString());
        }
    }

    /**
     * Send greeting to the room.
     *
     * @param session The websocket user session.
     */
    private void sendGreetings(final Session session) {

        if (session != null) {

            String room = "";
            String user = "";

            Map<String, Object> map = session.getUserProperties();
            if (map.containsKey("room")) {
                room = (String) session.getUserProperties().get("room");
            }
            if (map.containsKey("user")) {
                user = (String) session.getUserProperties().get("user");
            }
//            String msg = "<b>" + user + "</b>" + " has entered the room: " + room;
            String msg = "<b>" + user + "</b>" + " has joined the chat";
            Date received = new java.util.Date();

            /**
             * Collect the users nicknames which are connected to this room.
             */
            Set<String> userList = new LinkedHashSet<>();

            for (Session ses : nickNames.keySet()) {
                String userRoom = (String) ses.getUserProperties().get("room");
                String notifyRoom = (String) session.getUserProperties().get("room");

                if (userRoom.equals(notifyRoom)) {
                    userList.add(nickNames.get(ses));
                }
            }

            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setMessage(msg);
            chatMsg.setSender(user);
            chatMsg.setReceived(received);
            chatMsg.setNicknames(userList);

            // notify all other connected users
            onMessage(session, chatMsg);

            // send to user self to get the user nicknames list
            session.getAsyncRemote().sendObject(chatMsg);
        }
    }
}
