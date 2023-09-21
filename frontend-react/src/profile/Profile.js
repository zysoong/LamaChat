import React, { useEffect } from "react";
import { Card, Avatar } from "antd";
import { useRecoilState } from "recoil";
import { loggedInUser } from "../atom/globalState";
import { LogoutOutlined } from "@ant-design/icons";
import { getMe, logout } from "../util/ApiUtil";
import "./Profile.css"
import secureLocalStorage from "react-secure-storage";

const { Meta } = Card;

const Profile = (props) => {

    const [currentUser, setLoggedInUser] = useRecoilState(loggedInUser);

    useEffect(() => {
        if (secureLocalStorage.getItem("accessToken") === null) {
            props.history.push("/");
        }

        else {
            getMe()
                .then((responseText) => {
                    console.log("responseText=" + responseText)
                    setLoggedInUser(responseText)
                })
                .catch((error) => {
                    props.history.push("/");
                });
        }



    }, [setLoggedInUser, props.history]);


    const logoutOnClick = () => {
        logout().then(() => {
            secureLocalStorage.removeItem("accessToken");
            secureLocalStorage.removeItem("loggedUser");
            props.history.push("/");
        })
    };

    return (
        <div className="profile-container">
            <Card
                style={{ width: 420, border: "1px solid #e1e0e0" }}
                actions={[<LogoutOutlined onClick={logoutOnClick} />]}
            >
                <Meta
                    avatar={
                        <Avatar
                            src={currentUser.profilePicture}
                            className="user-avatar-circle"
                        />
                    }
                    title={currentUser.name}
                    description={"@" + currentUser.username}
                />
            </Card>
        </div>
    );
};

export default Profile;