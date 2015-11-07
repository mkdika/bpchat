package com.mkdika.bpchat.ui.servlet.util;

import com.mkdika.bpchat.entity.ChatMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author Maikel Chandika <mkdika@gmail.com>
 */
public class ChatMessageEncoder implements Encoder.Text<ChatMessage> {

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String encode(ChatMessage chatMessage) throws EncodeException {
        JsonArray array = Json.createArrayBuilder().build();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<String> users = new LinkedHashSet<>();
        users.addAll(chatMessage.getNicknames());

        for (String str : users) {
            arrayBuilder.add(str);
        }

        array = arrayBuilder.build();

        // format the date
        Date date = chatMessage.getReceived();
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        String dateStr = formatter.format(date);

        return Json.createObjectBuilder().add("message", chatMessage.getMessage()).add("sender", chatMessage.getSender())
                .add("received", dateStr).add("nicknames", array).build().toString();
    }
}
