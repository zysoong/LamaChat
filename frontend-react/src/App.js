import React from "react";
import { HashRouter, Route, Switch} from "react-router-dom";
import SignIn from "./signin/SignIn";
import "./App.css";
import Profile from "./profile/Profile";
import Chat from "./chat/Chat";
import Signup from "./signup/Signup";

export const AppContext = React.createContext();
const App = (props) => {
    return (
        <div className="App">
            <HashRouter>
                <Switch>
                    <Route exact path="/profile" render={(props) => <Profile {...props} />} />
                    <Route exact path="/" render={(props) => <SignIn {...props} />}/>
                    <Route exact path="/chat" render={(props) => <Chat {...props} />}/>
                    <Route exact path="/signup" render={(props) => <Signup {...props} />}/>
                </Switch>
            </HashRouter>
        </div>
    );
};

export default App;
