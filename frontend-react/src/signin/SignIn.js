import React, {useEffect, useState} from "react";
import {Button, Divider, Form, Input, notification} from "antd";
import {DingtalkOutlined, LockOutlined, UserOutlined,} from "@ant-design/icons";
import {loginWithToken} from "../util/ApiUtil";
import "./Signin.css";
import secureLocalStorage from "react-secure-storage";
import logo from './lama.png'


const SignIn = (props) => {

    const [loading, setLoading] = useState(false);

    useEffect(() => {

        if (secureLocalStorage.getItem("accessToken") !== null && secureLocalStorage.getItem("loggedUser") !== null) {

            setLoading(true);

            loginWithToken(secureLocalStorage.getItem("accessToken"))
                .then(() => {
                    props.history.push("/chat")
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
    }, [props.history]);

    const onFinish = (values) => {

        setLoading(true);

        loginWithToken(btoa(values.username + ':' + values.password))
            .then(() => {
                props.history.push("/chat");
                setLoading(false);
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
                Not a member yet? <a href="/signup">Sign up</a>
            </Form>
        </div>
    );
};

export default SignIn;