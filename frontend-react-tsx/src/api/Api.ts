import { notification } from "antd";
import secureLocalStorage from "react-secure-storage";
import axios from 'axios';

const host : string = import.meta.env.VITE_APP_SERVER_HOST;

const AUTH_SERVICE = "http://" + host;
const CHAT_SERVICE = "http://" + host;

type Headers = {
    'Content-Type': string;
    'Accept': string;
    'Authorization': string;
};

type RequestData = {
    url: string;
    method: 'GET' | 'POST' | 'PUT' | 'DELETE'; // Specify the HTTP method you need
    data?: Record<string, unknown>;
    headers?: Record<string, string>;
    credentials?: string;
}

type AppUserRequest = {
    userName: string,
    password: string,
    isVirtualAgent: boolean,
    api: string,
    apiKey: string
}

const request = (options : RequestData) => {

    const headers: Headers = {
        'Content-Type': '',
        'Accept': '',
        'Authorization': '',
    };

    if (secureLocalStorage.getItem("accessToken") !== null){
        headers.Authorization = "Basic " + secureLocalStorage.getItem("accessToken");
    };

    headers.Accept = "application/json";
    headers["Content-Type"] = "application/json";

    options.headers = headers;

    return axios(options)
        .then((response) => {
            if (response.status >= 200 && response.status < 300) {
                return response.data;
            } else {
                return Promise.reject(response.data);
            }
        });
};


const loginBasicAuth = (token: string) => {

    const headers: Headers = {
        'Content-Type': '',
        'Accept': '',
        'Authorization': "Basic " + token,
    };

    secureLocalStorage.setItem("accessToken", token);

    const defaults = { headers: headers };
    const options = Object.assign({}, defaults, {
        url: AUTH_SERVICE + "/api/auth/login",
        method: "POST",
        credentials: "include"
    });

    return axios(options)
        .then((response) => {
            if (response.status >= 200 && response.status < 300) {
                return response.data;
            } else {
                return Promise.reject(response.data);
            }
        })
        .then(() => {return getMe();})
        .then( (me) => {
                //findUserByUserName(me).then()
                notification.success({
                    message: "Info",
                    description: "User " + me + " has successfully logged in. ",
                })
                secureLocalStorage.setItem("loggedUser", "" + me);
            }
        )
        .catch(
            (error) => {
                notification.error({
                    message: "Error",
                    description:
                        error.message || "Authentication session invalid. Please check your user name or password. ",
                })
                secureLocalStorage.removeItem("accessToken")
                secureLocalStorage.removeItem("loggedUser");
            }
        );

}

export function loginWithToken(token: string){
    return loginBasicAuth(token);
}

export function getMe() {

    const options: RequestData = {
        url: AUTH_SERVICE + "/api/auth/me",
        method: "GET",
    };

    return request(options);
}

export function getMyContacts() {

    const options: RequestData = {
        url: AUTH_SERVICE + "/api/auth/me/contacts",
        method: "GET"
    };

    return request(options);
}

export function logout() {


    const options: RequestData = {
        url: AUTH_SERVICE + "/api/auth/logout",
        method: "POST"
    };

    return request(options);
}

export function signup(signupRequest: AppUserRequest) {


    const options: RequestData = {
        url: AUTH_SERVICE + "/api/auth/register",
        method: "POST",
        data: signupRequest
    };

    return request(options);
}



export function findUserByUserName(userName: string) {

    const options: RequestData = {
        url: AUTH_SERVICE + "/api/auth/" + userName,
        method: "GET"
    };

    return request(options)
        .then((json) => {return json})
}

export function findOrAddChatSessionByParticipantIds(id1: string, id2: string) {

    const options: RequestData = {
        url: CHAT_SERVICE + "/api/chatsessions/" + id1 + "/" + id2,
        method: "POST"
    };

    return request(options)
        .then((json) => {return json})
}