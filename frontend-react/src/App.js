import React from "react";
import {BrowserRouter, Route, Switch} from "react-router-dom";
import Signin from "./signin/Signin";
import "./App.css";

export const AppContext = React.createContext();
const App = (props) => {
    return (
        <div className="App">
            <BrowserRouter>
                <Switch>
                    <Route exact path="/" render={(props) => <Signin {...props} />}/>
                </Switch>
            </BrowserRouter>
        </div>
    );
};

export default App;
