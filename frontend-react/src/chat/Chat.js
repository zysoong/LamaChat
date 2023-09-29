import React, { useEffect, useRef, useState} from "react";
import {Button, notification} from "antd";
import {
    getMe, findUserByUserName, findOrAddChatSessionByParticipantIds, getMyContacts, logout,
} from "../util/ApiUtil";
import {  useRecoilState } from "recoil";
import {
    loggedInUser
} from "../atom/globalState";
import ScrollToBottom from "react-scroll-to-bottom";
import "./Chat.css";
import secureLocalStorage from "react-secure-storage";
import {createImageFromInitials, getRandomColor} from "../util/ImageUtil";

const host = process.env.REACT_APP_SERVER_HOST;

let stompClient = null;
const Chat = (props) => {

    const [currentUser, setCurrentUser] = useRecoilState(loggedInUser);
    const [text, setText] = useState("");
    const [sessionPartners, setSessionPartners] = useState([]);
    const [activeSessionPartnerID, setActiveSessionPartnerID] = useState(undefined);
    const activeSessionPartnerID_Ref = useRef(undefined);
    const [messages, setMessages] = useState([]);
    const isExistingContact = useRef(false)
    const [isAdding, setIsAdding] = useState(false)
    const [addUserText, setAddUserText] = useState("")

    useEffect( () => {
        isExistingContact.current = (sessionPartners.map(partner => partner.userId).includes(activeSessionPartnerID))
        activeSessionPartnerID_Ref.current = activeSessionPartnerID
    }, [activeSessionPartnerID, sessionPartners])

    const onMessageReceived = (msg) => {

        isExistingContact.current = (sessionPartners.map(partner => partner.userId).includes(JSON.parse(msg.body).senderId))

        if (!isExistingContact.current)
        {
            getMyContacts().then((users) => {
                setSessionPartners(users);
            })
        }
        if (activeSessionPartnerID_Ref.current === JSON.parse(msg.body).senderId)
        {
            setMessages(messages => [...messages, JSON.parse(msg.body)])
        }
    }

    const onConnected = () => {

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
    };

    const loadContacts = () => {
        getMyContacts().then((users) => {
            setSessionPartners(users);
            if (activeSessionPartnerID === undefined && users.length > 0) {
                setActiveSessionPartnerID(users[0].userId)
                activeSessionPartnerID_Ref.current = users[0].userId
            }
        });
    };


    const connect = () => {
        const Stomp = require("stompjs");
        let SockJS = require("sockjs-client");
        SockJS = new SockJS("http://" + host + "/ws");
        stompClient = Stomp.over(SockJS);
        stompClient.connect({}, onConnected, onError);
    };

    const onError = (err) => {
        const errMsg = "[ERROR]" + err
        console.log(errMsg)
        if (errMsg.includes("Lost connection"))
        {
            connect();
            loadContacts();
        }
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
            newMessages.push(message);
            setMessages(newMessages);
        }
    };

    const logoutOnClick = () => {
        logout().then(() => {
            secureLocalStorage.removeItem("accessToken");
            secureLocalStorage.removeItem("loggedUser");
            props.history.push("/");
        })
    };

    const handleAddUser = () => {

        let promise_findMyId = new Promise((resolve) => {
            findUserByUserName(secureLocalStorage.getItem("loggedUser"))
                .then((user) => { return user.userId })
                .then((userId) => { resolve(userId) ; })
        })

        let promise_findAddingUserId = new Promise((resolve) => {
            findUserByUserName(addUserText)
                .then((user) => { return user.userId })
                .then((userId) => { resolve(userId) ; })
                .catch((error) => {
                    notification.error({
                        message: "Error",
                        description: "User " + addUserText + " not found",
                    })
                })
        })

        Promise.all([promise_findMyId, promise_findAddingUserId])
            .then((results) => {
                return findOrAddChatSessionByParticipantIds(results[0], results[1])
            })
            .then((chatSession) => {
                return chatSession.chat_messages
            })
            .then((messages) => {
                setMessages(messages)
            })
            .then(() => {
                loadContacts()
            })
    }

    useEffect(() => {
        if (secureLocalStorage.getItem("accessToken") === null) {
            props.history.push("/");
        } else {
            connect();
            loadContacts();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [props.history]);

    useEffect(() => {

        if (activeSessionPartnerID === undefined) {
            console.log("Failed to get messages: active contact invalid")
            return;
        }
        else {

            setMessages([])

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
                            src={createImageFromInitials(500, currentUser.userName, getRandomColor())}
                            className="online"
                            alt=""
                        />
                        <p>{"Welcome to LamaChat, " + currentUser.userName + " !"}</p>
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
                                    activeSessionPartnerID && partner.userId === activeSessionPartnerID
                                        ? "contact active"
                                        : "contact"
                                }
                            >
                                <div className="wrap">
                                    <span className="contact-status online"></span>
                                    <img id={partner.userId} src={createImageFromInitials(500, partner.userName, getRandomColor())} alt="" />
                                    <div className="meta">
                                        <p className="name">{partner.userName}</p>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="container">

                    {isAdding &&
                        <div id="bottom-bar-add">
                            <input
                                id="add_user_input"
                                type="text"
                                placeholder="Enter user name"
                                value={addUserText}
                                onChange={(event) => {setAddUserText(event.target.value)}}
                            />
                            <button
                                id="confirmAddContact"
                                onClick={() => { handleAddUser(); setIsAdding(false) }}
                            >
                                <i className="fa fa-check fa-fw" aria-hidden="true"></i>
                                OK
                            </button>
                        </div>
                    }

                    <div id="bottom-bar">
                        <button id="addcontact" onClick={() => { setIsAdding(true) }}>
                            <i className="fa fa-plus fa-fw" aria-hidden="true"></i>{" "}
                            <span>Add</span>
                        </button>
                        <button id="settings" onClick={logoutOnClick}>
                            <i className="fa fa-sign-out fa-fw" aria-hidden="true"></i>{" "}
                            <span>Logout</span>
                        </button>
                    </div>
                </div>
            </div>
            <div className="content">
                <div className="contact-profile">
                    <p>{}</p>
                </div>
                <ScrollToBottom className="messages">
                    <ul>
                        {messages.map((msg, i) => (
                            <li
                                key={i}
                                className={msg.senderId === currentUser.userId ? "sent" : "replies"}>
                                <p>{msg.content}</p>
                            </li>
                        ))}
                    </ul>
                </ScrollToBottom>
                {sessionPartners.length > 0 && (
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
                            disabled={sessionPartners.length === 0}
                        />
                    </div>
                </div>
                )}
            </div>
        </div>
    );
};

export default Chat;