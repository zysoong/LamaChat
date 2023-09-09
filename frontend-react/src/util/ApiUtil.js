import { notification } from "antd";

const AUTH_SERVICE = "http://localhost:8080";
const CHAT_SERVICE = "http://localhost:8080";

const request = (options) => {
  const headers = new Headers();

  if (options.setContentType !== false) {
    headers.append("Content-Type", "application/json");
  }

  headers.append("Accept", "application/json");

  if (localStorage.getItem("sessionId")) {
    headers.append(
      "Cookie",
      "JSESSIONID= " + localStorage.getItem("sessionId")
    );
  }

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
        body: ""
    });

    return fetch(options.url, options).then((response) => 
        {

          if (!response.ok) {
            console.log("BBBB")
            return Promise.reject(response);
          }

          else if (response.ok){

            console.log("AAAAAAAA")
            console.log(localStorage.getItem("loggedUser").toString())

            localStorage.setItem("loggedUser", response.body)

            const cookieHeaderStr = response.headers.get('Set-Cookie');
            localStorage.setItem("sessionId", cookieHeaderStr.substring(11, cookieHeaderStr.indexOf(";")))

            console.log(response)
            console.log(localStorage.getItem("loggedUser"))

            console.log("AAfdsfsdAAAAAA")

            //return response.json();

          }
        }).catch((error) => {
          if (error.status === 401) {
            notification.error({
              message: "Error",
              description: "Username or Password is incorrect. Please try again!",
            });
          } 
          
          else {
            notification.error({
              message: "Error",
              description:
                error.message || "Sorry! Something went wrong. Please try again!",
            });
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

export function getCurrentUser() {
  if (!localStorage.getItem("accessToken")) {
    return Promise.reject("No access token set.");
  }

  return request({
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