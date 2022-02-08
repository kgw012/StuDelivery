import firebase from "firebase/compat/app";
// import database from "firebase/database";

var firebaseConfig = {
  apiKey: "AIzaSyBPBQofP4OergJ-yK8YYvZH4l3nJojF0kg", // Add API Key
  databaseURL:
    "https://study-meet-97589-default-rtdb.asia-southeast1.firebasedatabase.app/", // Add databaseURL
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

export const db = firebase;

var firepadRef = firebase.database().ref();

export const userName = prompt("What's your name?");
const urlparams = new URLSearchParams(window.location.search);
const roomId = urlparams.get("id");

if (roomId) {
  firepadRef = firepadRef.child(roomId);
} else {
  firepadRef = firepadRef.push();
  window.history.replaceState(null, "Meet", "?id=" + firepadRef.key);
}

export default firepadRef;
