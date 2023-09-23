package com.example.frontendjavafx.controller;

import com.example.frontendjavafx.model.ChatMessage;
import com.example.frontendjavafx.model.ChatMessageSend;
import com.example.frontendjavafx.model.ChatSession;
import com.example.frontendjavafx.security.AppUser;
import com.example.frontendjavafx.service.ChatViewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import java.io.IOException;
import java.lang.reflect.Type;
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
    private TextField addUser_TF;

    private StompSession stompSession;
    private ObjectMapper objectMapper = new ObjectMapper();

    private List<AppUser> myContacts;
    private List<String> namesOfMyContacts;
    private Map<String, String> mapFromUsernameToUserId = new HashMap<>();
    private Map<String, String> mapFromUserIdToUserName = new HashMap<>();
    private String myId;
    private String selectedUserId;


    public void initialize() {

        updateSessionList();

        if (myContacts.size() > 0)
        {
            changeChatPartner(myContacts.get(0).userId());
        }

        sessionList_LV
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                {
                    if (newValue != null) {
                        selectedUserId = ChatViewService.getInstance().getUserByUserName(newValue).userId();
                        changeChatPartner(selectedUserId);
                    }
                });

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

                updateSessionList();

                ChatMessage msgReceived;

                try {
                    msgReceived = objectMapper.readValue((byte[])payload, ChatMessage.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Received: " + msgReceived);
                String rcvMessageSenderName = mapFromUserIdToUserName.get(msgReceived.senderId());
                String selectedPartnerName = mapFromUserIdToUserName.get(selectedUserId);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (rcvMessageSenderName == selectedPartnerName){
                            chatContentList_LV.getItems().add(rcvMessageSenderName + ": " + msgReceived.content());
                        }
                    }
                });
            }
        };

        this.stompSession
                = stompClient.connectAsync(serverUrl, sessionHandler).join();

        System.out.println("Connected");

    }


    private void changeChatPartner(String partnerUserId){

        String partnerName = mapFromUserIdToUserName.get(partnerUserId);

        chatContentList_LV.getItems().clear();

        ChatSession selectedChatSession = ChatViewService
                .getInstance()
                .findOrAddChatSessionByParticipantIds(
                        myId,
                        partnerUserId
                );

        for (ChatMessage msg : selectedChatSession.chat_messages())
        {
            if (msg.senderId().equals(myId))
            {
                chatContentList_LV.getItems().add("me : " + msg.content());
            }
            else
            {
                chatContentList_LV.getItems().add(partnerName + ": " + msg.content());
            }
        }
    }

    private void updateSessionList(){

        this.myContacts = ChatViewService.getInstance().getMyContacts();
        this.namesOfMyContacts = new ArrayList<>();
        this.myId = ChatViewService.getInstance().getMeAsUserObject().userId();

        for (AppUser contact : myContacts){
            namesOfMyContacts.add(contact.userName());
            mapFromUsernameToUserId.put(contact.userName(), contact.userId());
            mapFromUserIdToUserName.put(contact.userId(), contact.userName());
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sessionList_LV.getItems().clear();
                sessionList_LV.getItems().addAll(namesOfMyContacts);
            }
        });



    }

    @FXML
    public void onClick_send(ActionEvent event) throws IOException {

        String recipientId = "";


        if (ChatViewService.getInstance().getMyContacts().size() == 0) {
            return;
        }
        if (sessionList_LV.getSelectionModel().getSelectedItems().size() == 0){
            recipientId = this.myContacts.get(0).userId();
        } else {
            String recipientName = sessionList_LV.getSelectionModel().getSelectedItems().get(0);
            recipientId = mapFromUsernameToUserId.get(recipientName);
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

    @FXML
    public void onClick_AddChatPartner (ActionEvent event) throws IOException
    {
        String searchUserName = this.addUser_TF.getText();
        String searchUserId = ChatViewService.getInstance().getUserByUserName(searchUserName).userId();
        ChatViewService.getInstance().findOrAddChatSessionByParticipantIds(myId, searchUserId);
        updateSessionList();
    }


}
