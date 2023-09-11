import React, {useCallback, useEffect, useState} from "react";
import { Button } from "antd";
import {
    getMe, findUserByUserName, findOrAddChatSessionByParticipantIds, getMyContacts,
} from "../util/ApiUtil";
import { useRecoilValue, useRecoilState } from "recoil";
import {
    loggedInUser,
    chatMessages,
} from "../atom/globalState";
import ScrollToBottom from "react-scroll-to-bottom";
import "./Chat.css";
import secureLocalStorage from "react-secure-storage";

let stompClient = null;
const Chat = (props) => {

    const currentUser = useRecoilValue(loggedInUser);
    const [text, setText] = useState("");
    const [sessionPartners, setSessionPartners] = useState([]);
    const [activeSessionPartnerID, setActiveSessionPartnerID] = useState(undefined);
    const [messages, setMessages] = useState([]);

    const connect = useCallback(() => {
        const Stomp = require("stompjs");
        let SockJS = require("sockjs-client");
        SockJS = new SockJS("http://localhost:8080/ws");
        stompClient = Stomp.over(SockJS);
        stompClient.connect({}, onConnected, onError);
    }, []);

    const loadContacts = useCallback(() => {
        getMyContacts().then((users) => {
            setSessionPartners(users);
            if (activeSessionPartnerID === undefined && users.length > 0) {
                setActiveSessionPartnerID(users[0].userId);
            }
        });
    }, []);

    useEffect(() => {
        if (secureLocalStorage.getItem("accessToken") === null) {
            props.history.push("/");
        } else {
            connect();
            loadContacts();
        }
    }, [props.history, loadContacts, connect]);

    useEffect(() => {

        if (activeSessionPartnerID === undefined) {
            console.log("Failed to get messages: active contact invalid")
            return;
        }
        else {
            getMe()
                .then((me) => {
                    return findUserByUserName(me)
                })
                .then((user) => {
                    return user.userId;
                })
                .then((userId) => {
                    return findOrAddChatSessionByParticipantIds(activeSessionPartnerID, userId)
                })
                .then((chatSession) => {
                    console.log("messages:::: alpha ->" + chatSession)
                    return chatSession.chat_messages
                })
                .then((messages) => {
                    setMessages(messages)
                    console.log("messages:::: " + messages)
                })
        }
    }, [activeSessionPartnerID, setMessages]);



    const onConnected = () => {
        console.log("connected");

        getMe()
            .then((me) => {
                return findUserByUserName(me)
            })
            .then((data) => {
                stompClient.subscribe(
                    "/user/" + data.userId + "/queue/messages",
                    onMessageReceived
                );
            })
    };

    const onError = (err) => {
        console.log(err);
    };

    const onMessageReceived = (msg) => {
        console.log("msg:::: RECEIVED" +  msg)
    };

    const sendMessage = (msg) => {
        if (msg.trim() !== "") {
            const message = {
                senderId: currentUser.id,
                recipientId: activeSessionPartnerID.id,
                senderName: currentUser.name,
                recipientName: activeSessionPartnerID.name,
                content: msg,
                timestamp: new Date(),
            };
            stompClient.send("/app/chat", {}, JSON.stringify(message));


            // TODO do promise here: 1-> add msg locally  2. sync messages from remote
            const newMessages = [...messages];
            newMessages.push(message);
            setMessages(newMessages);
        }
    };



    return (
        <div id="frame">
            <div id="sidepanel">
                <div id="profile">
                    <div class="wrap">
                        <img
                            id="profile-img"
                            src={currentUser.profilePicture}
                            class="online"
                            alt=""
                        />
                        <p>{currentUser.name}</p>
                        <div id="status-options">
                            <ul>
                                <li id="status-online" class="active">
                                    <span class="status-circle"></span> <p>Online</p>
                                </li>
                                <li id="status-away">
                                    <span class="status-circle"></span> <p>Away</p>
                                </li>
                                <li id="status-busy">
                                    <span class="status-circle"></span> <p>Busy</p>
                                </li>
                                <li id="status-offline">
                                    <span class="status-circle"></span> <p>Offline</p>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div id="search" />
                <div id="contacts">
                    <ul>
                        {sessionPartners.map((partner) => (
                            <li
                                onClick={() => {setActiveSessionPartnerID(partner.userId); console.log("Active contact set: " + activeSessionPartnerID)}}
                                class={
                                    activeSessionPartnerID && partner.userId === activeSessionPartnerID.userId
                                        ? "contact active"
                                        : "contact"
                                }
                            >
                                <div class="wrap">
                                    <span class="contact-status online"></span>
                                    <img id={partner.userId} src={partner.profilePicture} alt="" />
                                    <div class="meta">
                                        <p class="name">{partner.userName}</p>

                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
                <div id="bottom-bar">
                    <button id="addcontact">
                        <i class="fa fa-user fa-fw" aria-hidden="true"></i>{" "}
                        <span>Profile</span>
                    </button>
                    <button id="settings">
                        <i class="fa fa-cog fa-fw" aria-hidden="true"></i>{" "}
                        <span>Settings</span>
                    </button>
                </div>
            </div>
            <div class="content">
                <div class="contact-profile">
                    <img src={activeSessionPartnerID && activeSessionPartnerID.profilePicture} alt="" />
                    <p>{activeSessionPartnerID}</p>
                </div>
                <ScrollToBottom className="messages">
                    <ul>
                        {messages.map((msg) => (
                            <li class={msg.senderId === currentUser.id ? "sent" : "replies"}>
                                {msg.senderId !== currentUser.id && (
                                    <img src={activeSessionPartnerID.profilePicture} alt="" />
                                )}
                                <p>{msg.content}</p>
                            </li>
                        ))}
                    </ul>
                </ScrollToBottom>
                <div class="message-input">
                    <div class="wrap">
                        <input
                            name="user_input"
                            size="large"
                            placeholder="Write your message..."
                            value={text}
                            onChange={(event) => setText(event.target.value)}
                            onKeyPress={(event) => {
                                if (event.key === "Enter") {
                                    sendMessage(text);
                                    setText("");
                                }
                            }}
                        />

                        <Button
                            icon={<i class="fa fa-paper-plane" aria-hidden="true"></i>}
                            onClick={() => {
                                sendMessage(text);
                                setText("");
                            }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Chat;