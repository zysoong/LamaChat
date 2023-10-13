import { atom } from "recoil";

export const loggedInUser = atom({
    default: {},
    key: "loggedInUser",
    persistence_UNSTABLE: {
        type: "loggedInUser",
    },
});