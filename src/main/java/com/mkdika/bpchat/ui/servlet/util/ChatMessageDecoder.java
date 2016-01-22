package com.mkdika.bpchat.ui.servlet.util;

import com.mkdika.bpchat.entity.ChatMessage;
import java.io.StringReader;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author Maikel Chandika <mkdika@gmail.com>
 */
public class ChatMessageDecoder implements Decoder.Text<ChatMessage> {

    @Override
    public void init(final EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public ChatMessage decode(final String textMessage) throws DecodeException {
        ChatMessage chatMessage = new ChatMessage();
        JsonObject obj = Json.createReader(new StringReader(textMessage)).readObject();
        chatMessage.setMessage(obj.getString("message"));
        chatMessage.setSender(obj.getString("sender"));
        chatMessage.setReceived(new Date());
        System.out.println(">>>[DECODED] Sender:" + chatMessage.getSender() + ", message: " + chatMessage.getMessage());
        return chatMessage;
    }

    @Override
    public boolean willDecode(final String s) {
        return true;
    }
}
