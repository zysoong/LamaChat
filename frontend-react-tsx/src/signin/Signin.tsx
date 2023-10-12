import {Button, Divider, Form, Input} from "antd";
import { LockOutlined, UserOutlined,} from "@ant-design/icons";
import "./Signin.css";
import logo from "../assets/lama.png";

export default function Signin (){

    return (
        <div className="login-container">
            <img src={logo} alt="Logo" className="logo" />
            <h1 className="title">LamaChat</h1>
            <Form
                name="normal_login"
                className="login-form"
                initialValues={{remember: true}}
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