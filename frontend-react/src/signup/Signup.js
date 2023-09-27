import React, { useEffect, useState } from "react";
import {Form, Input, Button, notification, Checkbox} from "antd";
import { DingtalkOutlined } from "@ant-design/icons";
import {loginWithToken, signup} from "../util/ApiUtil";
import "./Signup.css";
import secureLocalStorage from "react-secure-storage";

const Signup = (props) => {

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

        if (!values.api) {
            values.api = '';
        }

        if (!values.apiKey) {
            values.apiKey = '';
        }

        if (!values.isVirtualAgent) {
            values.isVirtualAgent = false;
        }

        signup(values)
            .then(() => {
                notification.success({
                    message: "Success",
                    description:
                        "Thank you! You're successfully registered. Please Login to continue!",
                });
                props.history.push("/");
                setLoading(false);
            })
            .catch((error) => {
                notification.error({
                    message: "Error",
                    description:
                        error.message || "Sorry! User name was already registered",
                });
                setLoading(false);
            });
    };

    return (
        <div className="login-container">
            <DingtalkOutlined style={{ fontSize: 50 }} />

            <Form
                name="normal_login"
                className="login-form"
                initialValues={{ remember: true }}
                onFinish={onFinish}
            >

                <Form.Item
                    name="userName"
                    rules={[{ required: true, message: "Please input your Username!" }]}
                >
                    <Input size="large" placeholder="Username" />
                </Form.Item>

                <Form.Item
                    name="password"
                    rules={[{ required: true, message: "Please input your Password!" }]}
                >
                    <Input size="large" type="password" placeholder="Password" />
                </Form.Item>

                <Form.Item
                    name="api"
                    rules={[{ required: false, message: "Please input your API URL!" }]}
                >
                    <Input size="large" placeholder="API" />
                </Form.Item>

                <Form.Item
                    name="apiKey"
                    rules={[{ required: false, message: "Please input your API Key!" }]}
                >
                    <Input size="large" placeholder="API Key" />
                </Form.Item>

                <Form.Item
                    name="isVirtualAgent"
                    label="Are you a bot?"
                    valuePropName="checked"
                >
                    <Checkbox />
                </Form.Item>

                <Form.Item>
                    <Button
                        shape="round"
                        size="large"
                        htmlType="submit"
                        className="login-form-button"
                        loading={loading}
                    >
                        Signup
                    </Button>
                </Form.Item>
                Already a member? <a href="/">Log in</a>
            </Form>
        </div>
    );
};

export default Signup;