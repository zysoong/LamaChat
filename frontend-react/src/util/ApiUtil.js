import { notification } from "antd";
const AUTH_SERVICE = "http://localhost:8080";
const CHAT_SERVICE = "http://localhost:8080";

const request = (options) => {
  const headers = new Headers();

  if (options.setContentType !== false) {
    headers.append("Content-Type", "application/json");
  }

  headers.append("Accept", "application/json");

  const defaults = { headers: headers };
  options = Object.assign({}, defaults, options);

  return fetch(options.url, options).then((response) =>
    response.json().then((json) => {
      if (!response.ok) {
        return Promise.reject(json);
      }
      return json;
    })
  );
};


const requestText = (options) => {
  const headers = new Headers();

  if (options.setContentType !== false) {
    headers.append("Content-Type", "application/json");
  }

  //headers.append("Accept", "application/json");

  if (localStorage.getItem("sessionId")) {
    headers.append(
      "Cookie",
      "JSESSIONID= " + localStorage.getItem("sessionId")
    );
  }

  const defaults = { headers: headers };
  options = Object.assign({}, defaults, options);

  return fetch(options.url, options).then((response) =>
    response.text().then((json) => {
      if (!response.ok) {
        return Promise.reject(json);
      }
      return json;
    })
  );
};


const loginBasicAuth = (token) => {

    const headers = new Headers();

    headers.append(
        "Authorization",
        "Basic " + token
    );

    const defaults = { headers: headers };
    const options = Object.assign({}, defaults, {
        url: AUTH_SERVICE + "/api/auth/login",
        method: "POST", 
        credentials: "include"
    });

    return fetch(options.url, options).then((response) => 
        {

          if (!response.ok) {
            return Promise.reject(response);
          }

          else if (response.ok){

            response.text().then(
              (data) => 
                {
                  return localStorage.setItem("loggedUser", data);
                }
            )
            .then( () =>
              notification.success({
                message: "Info",
                description: "User " + localStorage.getItem("loggedUser") + " has successfully logged in. ",
              })
            )
            .then( () => getMe()) //TODO only for testing
            .catch(
              (error) => notification.error({
                message: "Error",
                description:
                  error.message || "Login succeed but something went wrong.",
              })
            )
          }
        });

}

export function login(token){
  return loginBasicAuth(token);
}

export function signup(signupRequest) {
  return request({
    url: AUTH_SERVICE + "/api/auth/register",
    method: "POST",
    body: JSON.stringify(signupRequest),
  });
}

export function getMe() {
  return requestText({
    url: AUTH_SERVICE + "/api/auth/me",
    method: "GET",
  });
}


export function findChatMessages(senderId, recipientId) {
  if (!localStorage.getItem("accessToken")) {
    return Promise.reject("No access token set.");
  }

  return request({
    url: CHAT_SERVICE + "/messages/" + senderId + "/" + recipientId,
    method: "GET",
  });
}

export function findChatMessage(id) {
  if (!localStorage.getItem("accessToken")) {
    return Promise.reject("No access token set.");
  }

  return request({
    url: CHAT_SERVICE + "/messages/" + id,
    method: "GET",
  });
}