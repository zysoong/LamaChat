import { notification } from "antd";
import secureLocalStorage from "react-secure-storage";
const AUTH_SERVICE = "http://localhost:8080";
const CHAT_SERVICE = "http://localhost:8080";

const request = (options) => {
  const headers = new Headers();

  if (secureLocalStorage.getItem("accessToken") !== null){
    headers.append(
      "Authorization",
      "Basic " + secureLocalStorage.getItem("accessToken")
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

  if (secureLocalStorage.getItem("accessToken") !== null){
    headers.append(
      "Authorization",
      "Basic " + secureLocalStorage.getItem("accessToken")
    );
  }

  if (options.setContentType !== false) {
    headers.append("Content-Type", "application/json");
  }

  //headers.append("Accept", "application/json");

  if (secureLocalStorage.getItem("sessionId")) {
    headers.append(
      "Cookie",
      "JSESSIONID= " + secureLocalStorage.getItem("sessionId")
    );
  }

  const defaults = { headers: headers };
  options = Object.assign({}, defaults, options);

  return fetch(options.url, options).then((response) =>
    response.text().then((data) => {
      if (!response.ok) {
        return Promise.reject(data);
      }
      return data;
    })
  );
};


const loginBasicAuth = (token) => {

    const headers = new Headers();
    secureLocalStorage.setItem("accessToken", token);

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

            response.text()
            .then( () => {return getMe()})
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
            )
          }
        });

}

export function loginWithToken(token){
  return loginBasicAuth(token);
}

export function getMe() {
    return requestText({
        url: AUTH_SERVICE + "/api/auth/me",
        method: "GET",
    });
}

export function getMyContacts() {
    return request({
        url: AUTH_SERVICE + "/api/auth/me/contacts",
        method: "GET",
    });
}

export function logout() {
    return requestText({
        url: AUTH_SERVICE + "/api/auth/logout",
        method: "POST",
    });
}

export function signup(signupRequest) {
  return request({
    url: AUTH_SERVICE + "/api/auth/register",
    method: "POST",
    body: JSON.stringify(signupRequest),
  });
}



export function findUserByUserName(userName) {
  return request({
    url: AUTH_SERVICE + "/api/auth/" + userName,
    method: "GET",
  })
  .then((json) => {return json})
}

export function findOrAddChatSessionByParticipantIds(id1, id2) {
    return request({
        url: CHAT_SERVICE + "/api/chatsessions/" + id1 + "/" + id2,
        method: "POST",
    })
        .then((json) => {return json})
}