package com.example.frontendjavafx.controller;

import com.example.frontendjavafx.model.ChatMessage;
import com.example.frontendjavafx.model.ChatMessageSend;
import com.example.frontendjavafx.model.ChatSession;
import com.example.frontendjavafx.security.AppUser;
import com.example.frontendjavafx.service.ChatViewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.json.JSONObject;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.simp.stomp.StompSessionHandler;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.ZoneId;
import java.util.Date;

public class ChatViewController {

    @FXML
    private ListView<String> sessionList_LV;

    @FXML
    private Text welcomeText_T;

    @FXML
    private Button logoutButton_B;

    @FXML
    private ListView<String> chatContentList_LV;

    @FXML
    private TextArea sendChatContent_TV;

    @FXML
    private Button sendButton_B;

    private String URL_BACKEND = System.getenv("BACKEND_URI");

    private StompSession stompSession;

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<AppUser> myContacts;

    public void initialize() {

        this.myContacts = ChatViewService.getInstance().getMyContacts();
        List<String> namesOfMyContacts = new ArrayList<>();
        Map<String, String> mapFromUsernameToUserId = new HashMap<>();
        Map<String, String> mapFromUserIdToUserName = new HashMap<>();

        String myId = ChatViewService.getInstance().getMeAsUserObject().userId();

        for (AppUser contact : myContacts){
            namesOfMyContacts.add(contact.userName());
            mapFromUsernameToUserId.put(contact.userName(), contact.userId());
            mapFromUserIdToUserName.put(contact.userId(), contact.userName());
        }

        sessionList_LV.getItems().addAll(namesOfMyContacts);



        if (myContacts.size() > 0)
        {

            ChatSession firstChatSession = ChatViewService
                    .getInstance()
                    .findOrAddChatSessionByParticipantIds(
                            myId,
                            myContacts.get(0).userId()
                    );

            for (ChatMessage msg : firstChatSession.chat_messages())
            {
                if (msg.senderId().equals(myId))
                {
                    chatContentList_LV.getItems().add("me : " + msg.content());
                }
                else
                {
                    chatContentList_LV.getItems().add(myContacts.get(0).userName() + ": " + msg.content());
                }
            }
        }

        String serverUrl = "http://localhost:8080/ws";

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        StompSessionHandler sessionHandler = new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Subscribing: " + "/user/" + myId + "/queue/messages");
                session.subscribe("/user/" + myId + "/queue/messages", this);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                ChatMessage msgReceived;

                try {
                    msgReceived = objectMapper.readValue((byte[])payload, ChatMessage.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Received: " + msgReceived);
                String partnerName = mapFromUserIdToUserName.get(msgReceived.senderId());
                chatContentList_LV.getItems().add(partnerName + ": " + msgReceived.content());

            }
        };

        this.stompSession
                = stompClient.connectAsync(serverUrl, sessionHandler).join();

        System.out.println("Connected");

    }

    @FXML
    public void onClick_send(ActionEvent event) throws IOException {

        JSONObject chatMessageJson = new JSONObject();

        String recipientId = "";
        if (ChatViewService.getInstance().getMyContacts().size() == 0) {
            return;
        }
        if (sessionList_LV.getSelectionModel().getSelectedItems().size() == 0){
            recipientId = this.myContacts.get(0).userId();
        } else {
            recipientId = sessionList_LV.getSelectionModel().getSelectedItems().get(0);
        }

        LocalDate localDate = LocalDate.now();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        ChatMessageSend sendMsg = new ChatMessageSend(
                ChatViewService.getInstance().getMeAsUserObject().userId(),
                recipientId,
                date,
                sendChatContent_TV.getText()
        );

        byte[] payLoad = objectMapper.writeValueAsBytes(sendMsg);
        stompSession.send("/app/chat", payLoad);

        chatContentList_LV.getItems().add("me: " + sendMsg.content());
        int lastIndex = chatContentList_LV.getItems().size() - 1;
        if (lastIndex >= 0) {
            chatContentList_LV.scrollTo(lastIndex);
        }

        sendChatContent_TV.clear();

    }


}
