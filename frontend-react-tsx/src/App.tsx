//import { useState } from 'react'
//import reactLogo from './assets/react.svg'
//import viteLogo from '/vite.svg'
import './App.css'
import './hello/HelloWorld.tsx'
import HelloWorld from "./hello/HelloWorld.tsx";

function App() {

  //const [count, setCount] = useState(0)
  const bealtifulNames : string[] = ["Ziyang", "Jaro", "Dirk"]


  return (
    <>
        {bealtifulNames.map(name => (
            <HelloWorld name={name}/>
        ))}
    </>
  )
}

export default App
