type HelloWorldProps = {
    name: string
}

export default function HelloWorld(props: HelloWorldProps){

    return (
        <>
            Hello {props.name} !
        </>
    )

}