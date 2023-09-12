import React, {useCallback, useEffect, useState} from "react";
import { Button } from "antd";
import {
    getMe, findUserByUserName, findOrAddChatSessionByParticipantIds, getMyContacts,
} from "../util/ApiUtil";
import {  useRecoilState } from "recoil";
import {
    loggedInUser
} from "../atom/globalState";
import ScrollToBottom from "react-scroll-to-bottom";
import "./Chat.css";
import secureLocalStorage from "react-secure-storage";

let stompClient = null;
const Chat = (props) => {

    const [currentUser, setCurrentUser] = useRecoilState(loggedInUser);
    const [text, setText] = useState("");
    const [sessionPartners, setSessionPartners] = useState([]);
    const [activeSessionPartnerID, setActiveSessionPartnerID] = useState(undefined);
    const [messages, setMessages] = useState([]);

    const onMessageReceived = (msg) => {
        setMessages(messages => [...messages, JSON.parse(msg.body)])
    }

    const onConnected = useCallback(() => {
        console.log("connected");

        getMe()
            .then((me) => {
                return findUserByUserName(me)
            })
            .then((user) => {setCurrentUser(user); return user;})
            .then((data) => {
                stompClient.subscribe(
                    "/user/" + data.userId + "/queue/messages",
                    onMessageReceived
                );
            })
    }, [messages, setMessages, onMessageReceived, setCurrentUser]);

    const loadContacts = useCallback(() => {
        getMyContacts().then((users) => {
            setSessionPartners(users);
            if (activeSessionPartnerID === undefined && users.length > 0) {
                setActiveSessionPartnerID(users[0].userId);
            }
        });
    }, [activeSessionPartnerID]);


    const connect = useCallback(() => {
        const Stomp = require("stompjs");
        let SockJS = require("sockjs-client");
        SockJS = new SockJS("http://localhost:8080/ws");
        stompClient = Stomp.over(SockJS);
        stompClient.connect({}, onConnected, onError);
    }, []);

    const onError = (err) => {
        console.log(err);
    };

    const sendMessage = (msg) => {
        if (msg.trim() !== "") {
            const message = {
                senderId: currentUser.userId,
                recipientId: activeSessionPartnerID,
                timestamp: new Date(),
                content: msg
            };
            stompClient.send("/app/chat", {}, JSON.stringify(message));

            // TODO do promise here: 1-> add msg locally  2. sync messages from remote
            const newMessages = [...messages];
            console.log(newMessages)
            newMessages.push(message);
            console.log(newMessages)
            setMessages(newMessages);
        }
    };



    useEffect(() => {
        if (secureLocalStorage.getItem("accessToken") === null) {
            props.history.push("/");
        } else {
            connect();
            loadContacts();
        }
    }, [props.history]);

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
                    return chatSession.chat_messages
                })
                .then((messages) => {
                    setMessages(messages)
                })
        }
    }, [activeSessionPartnerID, setMessages]);


    return (
        <div id="frame">
            <div id="sidepanel">
                <div id="profile">
                    <div className="wrap">
                        <img
                            id="profile-img"
                            src={currentUser.profilePicture}
                            className="online"
                            alt=""
                        />
                        <p>{currentUser.name}</p>
                        <div id="status-options">
                            <ul>
                                <li id="status-online" className="active">
                                    <span className="status-circle"></span> <p>Online</p>
                                </li>
                                <li id="status-away">
                                    <span className="status-circle"></span> <p>Away</p>
                                </li>
                                <li id="status-busy">
                                    <span className="status-circle"></span> <p>Busy</p>
                                </li>
                                <li id="status-offline">
                                    <span className="status-circle"></span> <p>Offline</p>
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
                                key={partner.userId}
                                onClick={() => {setActiveSessionPartnerID(partner.userId); }}
                                className={
                                    activeSessionPartnerID && partner.userId === activeSessionPartnerID.userId
                                        ? "contact active"
                                        : "contact"
                                }
                            >
                                <div className="wrap">
                                    <span className="contact-status online"></span>
                                    <img id={partner.userId} src={partner.profilePicture} alt="" />
                                    <div className="meta">
                                        <p className="name">{partner.userName}</p>

                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
                <div id="bottom-bar">
                    <button id="addcontact">
                        <i className="fa fa-user fa-fw" aria-hidden="true"></i>{" "}
                        <span>Profile</span>
                    </button>
                    <button id="settings">
                        <i className="fa fa-cog fa-fw" aria-hidden="true"></i>{" "}
                        <span>Settings</span>
                    </button>
                </div>
            </div>
            <div className="content">
                <div className="contact-profile">
                    <img src={activeSessionPartnerID && activeSessionPartnerID.profilePicture} alt="" />
                    <p>{activeSessionPartnerID}</p>
                </div>
                <ScrollToBottom className="messages">
                    <ul>
                        {messages.map((msg) => (
                            <li
                                key={msg.messageId}
                                className={msg.senderId === currentUser.userId ? "sent" : "replies"}>
                                <p>{msg.content}</p>
                            </li>
                        ))}
                    </ul>
                </ScrollToBottom>
                <div className="message-input">
                    <div className="wrap">
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
                            icon={<i className="fa fa-paper-plane" aria-hidden="true"></i>}
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