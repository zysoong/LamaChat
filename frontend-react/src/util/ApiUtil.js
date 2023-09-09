import { notification } from "antd";
const AUTH_SERVICE = "http://localhost:8080";
const CHAT_SERVICE = "http://localhost:8080";

const request = (options) => {
  const headers = new Headers();

  if (localStorage.getItem("accessToken") !== null){
    headers.append(
      "Authorization",
      "Basic " + localStorage.getItem("accessToken")
    );
  }

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

  if (localStorage.getItem("accessToken") !== null){
    headers.append(
      "Authorization",
      "Basic " + localStorage.getItem("accessToken")
    );
  }

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
    localStorage.setItem("accessToken", token);

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
            
            .then( () => {return getMe()}) //TODO only for testing
            .then( (me) => {return findUserByUserName(me)})
            .catch(
              (error) => {
                notification.error({
                  message: "Error",
                  description:
                    error.message || "Login granted. Internal error. ",
                })
                localStorage.removeItem("accessToken")
                localStorage.removeItem("loggedUser")
              }
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

export function findUserByUserName(userName) {
  return request({
    url: AUTH_SERVICE + "/api/auth/" + userName,
    method: "GET",
  }).then((json) => console.log(json.userId));
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