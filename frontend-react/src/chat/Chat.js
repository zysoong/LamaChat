import React, { useEffect, useRef, useState} from "react";
import { Button, notification } from "antd";

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
    const [sessionPartners, setSessionPartners] = useState([]);
    const sessionPartners_Ref = useRef();
    sessionPartners_Ref.current = sessionPartners;
    const [activeSessionPartnerID, setActiveSessionPartnerID] = useState(undefined);
    const activeSessionPartnerID_Ref = useRef();
    activeSessionPartnerID_Ref.current = activeSessionPartnerID;

    const [chatText, setChatText] = useState("");
    const [messages, setMessages] = useState([]);
    const [notificationMap, setNotificationMap] = useState({});

    const [isAdding, setIsAdding] = useState(false);
    const [addUserText, setAddUserText] = useState("");

    const [imageMap, setImageMap] = useState({});
    const [ownImage, setOwnImage] = useState(undefined);
    const ownImageLoaded_Ref = useRef(false);


    const onMessageReceived = (msg) => {


        if (!sessionPartners_Ref.current.map(partner => partner.userId).includes(JSON.parse(msg.body).senderId))
        {
            getMyContacts()
                .then((users) =>
                {
                    const promise1 = new Promise((resolve) => {
                        setSessionPartners(users);
                        resolve();
                    })

                    const promise2 = new Promise((resolve) => {
                        users.map(user => {
                            if (user.userId === JSON.parse(msg.body).senderId){
                                setImageMap((prevImageMap) => ({
                                    ...prevImageMap,
                                    [JSON.parse(msg.body).senderId]: createImageFromInitials(500, user.userName, getRandomColor()),
                                }));
                            }
                            return undefined;
                        });
                        resolve();
                    })

                    Promise.all([promise1, promise2]).then();

                })
        }

        if (activeSessionPartnerID_Ref.current === JSON.parse(msg.body).senderId)
        {
            setMessages(messages => [...messages, JSON.parse(msg.body)])
        }
        else
        {
            setNotificationMap((prevNotificationMap) => ({
                ...prevNotificationMap,
                [JSON.parse(msg.body).senderId]: true,
            }));
        }
    }

    const onConnected = () => {

        console.log("connected");
        setCurrentUser({})

        getMe()
            .then((me) => {
                return findUserByUserName(me)
            })
            .then((user) => {
                setCurrentUser(user);
                return user;
            })
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

            users.map(user => {

                setImageMap((prevImageMap) => ({
                    ...prevImageMap,
                    [user.userId]: createImageFromInitials(500, user.userName, getRandomColor()),
                }));

                setNotificationMap((prevNotificationMap) => ({
                    ...prevNotificationMap,
                    [user.userId]: false,
                }));

                return undefined;
        })
            if (activeSessionPartnerID === undefined && users.length > 0) {
                setActiveSessionPartnerID(users[0].userId)
                activeSessionPartnerID_Ref.current = users[0].userId
            }
        })
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
            return;
        }
        else {

            setMessages([])


            getMe()
                .then((me) => {
                    return findUserByUserName(me)
                })
                .then((user) => {
                    if (!ownImageLoaded_Ref.current){
                        setOwnImage(createImageFromInitials(500, user.userName, getRandomColor()));
                        ownImageLoaded_Ref.current = true;
                    }
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

            setNotificationMap((prevNotificationMap) => ({
                ...prevNotificationMap,
                [activeSessionPartnerID]: false,
            }));

        }
    }, [activeSessionPartnerID, setMessages, ownImageLoaded_Ref, currentUser.userName]);


    return (
        <div id="frame">
            <div id="sidepanel">
                <div id="profile">
                    <div className="wrap">
                        <img
                            id="profile-img"
                            src={ownImage}
                            className="online"
                            alt=""
                        />
                        <p>{"Welcome, " + currentUser.userName + " !"}</p>
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

                                    {notificationMap[partner.userId] ? (
                                        <span className="contact-status online"></span>
                                    ) : null}

                                    <img id={partner.userId} src={imageMap[partner.userId]} alt=""/>
                                    <div className="meta">
                                        <p className="name">
                                            {partner.userName}
                                            {partner.isVirtualAgent ? (
                                                <i className="robot-icon">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"><path fill="currentColor" d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2M7.5 13A2.5 2.5 0 0 0 5 15.5A2.5 2.5 0 0 0 7.5 18a2.5 2.5 0 0 0 2.5-2.5A2.5 2.5 0 0 0 7.5 13m9 0a2.5 2.5 0 0 0-2.5 2.5a2.5 2.5 0 0 0 2.5 2.5a2.5 2.5 0 0 0 2.5-2.5a2.5 2.5 0 0 0-2.5-2.5Z"/></svg>
                                                </i>
                                            ) : null}
                                        </p>
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
                            value={chatText}
                            onChange={(event) => setChatText(event.target.value)}
                            onKeyPress={(event) => {
                                if (event.key === "Enter") {
                                    sendMessage(chatText);
                                    setChatText("");
                                }
                            }}
                        />

                        <Button
                            icon={<i className="fa fa-paper-plane" aria-hidden="true"></i>}
                            onClick={() => {
                                sendMessage(chatText);
                                setChatText("");
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