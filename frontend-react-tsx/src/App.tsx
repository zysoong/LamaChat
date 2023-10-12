import './App.css'
import './hello/HelloWorld.tsx'
import Signin from '../src/signin/Signin.tsx';
import { HashRouter, Routes, Route } from "react-router-dom";

function App() {

  return (
      <div className="App">
          <HashRouter>
              <Routes>
                  <Route path="/" element={<Signin />}/>
              </Routes>
          </HashRouter>
      </div>
  )
}

export default App
