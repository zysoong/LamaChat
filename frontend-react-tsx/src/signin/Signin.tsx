import { Button, Divider, Form, Input, notification } from "antd";
import { LockOutlined, UserOutlined,} from "@ant-design/icons";
import "./Signin.css";
import logo from "../assets/lama.png";
import {useEffect, useState} from "react";
import secureLocalStorage from "react-secure-storage";
import {loginWithToken} from "../api/Api.ts";
import {useNavigate} from "react-router-dom";

type LoginRequest = {
    username: string,
    password: string,
}

export default function Signin (){

    const [loading, setLoading] = useState(false);
    const history = useNavigate();

    useEffect(() => {

        if (secureLocalStorage.getItem("accessToken") !== null && secureLocalStorage.getItem("loggedUser") !== null) {

            setLoading(true);

            const token = secureLocalStorage.getItem("accessToken");
            let tokenString: string = "";

            if (typeof token === 'string') {
                tokenString = JSON.parse(token)

            }

            loginWithToken(tokenString)
                .then(() => {
                    history("/chat")
                    setLoading(false)
                })
                .catch((error) => {
                    if (error.status === 401) {
                        notification.error({
                            message: "Error",
                            description: "Invalid login",
                        })
                        secureLocalStorage.removeItem("accessToken");
                        secureLocalStorage.removeItem("loggedUser");
                    } else {
                        notification.error({
                            message: "Error",
                            description:
                                error.message || "Sorry! Something went wrong. Please try again!",
                        })
                        secureLocalStorage.removeItem("accessToken");
                        secureLocalStorage.removeItem("loggedUser");
                    }
                    setLoading(false);
                });

        }
    }, [history]);

    const onFinish = (values: LoginRequest) => {

        setLoading(true);

        const host : string = import.meta.env.VITE_APP_SERVER_HOST;
        console.log(host);

        loginWithToken(btoa(values.username + ':' + values.password))
            .then(() => {
                setLoading(false);
                history("/chat");
            })
            .catch((error) => {
                if (error.status === 401) {
                    notification.error({
                        message: "Error",
                        description: "Username or Password is incorrect. Please try again!",
                    });
                    secureLocalStorage.removeItem("accessToken");
                    secureLocalStorage.removeItem("loggedUser");
                } else {
                    notification.error({
                        message: "Error",
                        description:
                            error.message || "Sorry! Something went wrong. Please try again!",
                    });
                    secureLocalStorage.removeItem("accessToken")
                    secureLocalStorage.removeItem("loggedUser");
                }
                setLoading(false);
            });
    };

    return (
        <div className="login-container">
            <img src={logo} alt="Logo" className="logo" />
            <h1 className="title">LamaChat</h1>
            <Form
                name="normal_login"
                className="login-form"
                initialValues={{remember: true}}
                onFinish={onFinish}
            >
                <Form.Item
                    name="username"
                    rules={[{required: true, message: "Please input your Username!"}]}
                >
                    <Input
                        size="large"
                        prefix={<UserOutlined className="site-form-item-icon"/>}
                        placeholder="Username"
                    />
                </Form.Item>
                <Form.Item
                    name="password"
                    rules={[{required: true, message: "Please input your Password!"}]}
                >
                    <Input
                        size="large"
                        prefix={<LockOutlined className="site-form-item-icon"/>}
                        type="password"
                        placeholder="Password"
                    />
                </Form.Item>
                <Form.Item>
                    <Button
                        shape="round"
                        size="large"
                        htmlType="submit"
                        className="login-form-button"
                        loading={loading}
                    >
                        Log in
                    </Button>
                </Form.Item>
                <Divider>OR</Divider>
                Not a member yet? <a href="/#/signup">Sign up</a>
            </Form>
        </div>
    );
    
}