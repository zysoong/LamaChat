import React from "react";
import {BrowserRouter, Route, Switch} from "react-router-dom";
import SignIn from "./signin/SignIn";
import "./App.css";
import Profile from "./profile/Profile";
import Chat from "./chat/Chat";

export const AppContext = React.createContext();
const App = (props) => {
    return (
        <div className="App">
            <BrowserRouter>
                <Switch>
                    <Route exact path="/profile" render={(props) => <Profile {...props} />} />
                    <Route exact path="/" render={(props) => <SignIn {...props} />}/>
                    <Route exact path="/chat" render={(props) => <Chat {...props} />}/>
                </Switch>
            </BrowserRouter>
        </div>
    );
};

export default App;
