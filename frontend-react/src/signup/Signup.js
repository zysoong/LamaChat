import React, { useEffect, useState } from "react";
import {Form, Input, Button, notification, Checkbox} from "antd";
import {loginWithToken, signup} from "../util/ApiUtil";
import "./Signup.css";
import secureLocalStorage from "react-secure-storage";
import logo from './lama.png'


const Signup = (props) => {

    const [loading, setLoading] = useState(false);
    const [isBotChecked, setIsBotChecked] = useState(false);
    const [apiRules, setApiRules] = useState([
        { required: false, message: "" },
    ]);
    const [apiKeyRules, setApiKeyRules] = useState([
        { required: false, message: "" },
    ]);

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

    const onCheckboxChange = (e) => {
        
        setIsBotChecked(e.target.checked);

        const newApiRules = e.target.checked
            ? [{ required: true, message: "Please input your API url!" }]
            : [{ required: false, message: "" }];
        const newApiKeyRules = e.target.checked
            ? [{ required: false, message: "Please input your API key!" }]
            : [{ required: false, message: "" }];

        setApiRules(newApiRules)
        setApiKeyRules(newApiKeyRules)
    };

    return (
        <div className="login-container">
            <img src={logo} alt="Logo" className="logo"/>

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
                    rules={apiRules}
                >
                    <Input size="large" placeholder="API" disabled={!isBotChecked} />
                </Form.Item>

                <Form.Item
                    name="apiKey"
                    rules={apiKeyRules}
                >
                    <Input size="large" placeholder="API Key" disabled={!isBotChecked} />
                </Form.Item>

                <Form.Item
                    name="isVirtualAgent"
                    label="Are you a bot?"
                    valuePropName="checked"
                >
                    <Checkbox checked={isBotChecked} onChange={onCheckboxChange}/>
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