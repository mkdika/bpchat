/* global zk */

// Common vars
var wsocket;
var room = 'bpchat';
var serviceLocation = '';
var uid = localStorage.getItem("userid");
var serviceLocation = "ws://localhost:9898/bpchat/ws/";

function pops() {
    alert(uid);
}

/*
 * Check if browser supports WebSocket.
 */
function checkWebSocket() {
    if ("WebSocket" in window) {
        return true;
    } else {
        return false;
    }
}

/*
 * Check if string is empty, null or undefined.
 */
function isStrEmpty(_str) {
    return (!_str || 0 === _str.length);
}

/*
 * Connect to WebSocket Server.
 */
function connectToChatserver() {

    if (checkWebSocket() === false) {
        alert(zk('$errLbl_WebSocketNotSupported').$().getValue());
    }

    if (isStrEmpty(uid)) {
        alert(zk('$errLbl_UsernameMissing').$().getValue());
        return;
    }

    // Check if only a-z; A-Z, 0-9 is inserted as nickname
    if (!/^[0-9a-zA-Z]+$/.test(uid)) {
        alert(zk('$errLbl_FalseCharacterForUsername').$().getValue());
        return;
    }

    wsocket = new WebSocket(serviceLocation + room + '?user=' + uid);
    wsocket.onmessage = onMessageReceived;
    wsocket.onopen = function () {
        wsocket;
    };

    wsocket.onerror = function (error) {        
        var state = error.currentTarget.readyState;
        var err;
        if (state === 0) {
            err = 'Socket.readyState = ' + state + '\n\nConnection has not yet been established.';
            console.log(err);
        } else if (state === 2) {
            err = 'Socket.readyState = ' + state + '\n\nConnection is going through the closing handshake.';
            console.log(err);
        } else if (state === 3) {
            err = 'Socket.readyState = ' + state + '\n\nConnection has been closed or could not be opened.';
            console.log(err);
        }

        if (err.length !== 1) {
            alert(err);
        }
    };

    wsocket.onclose = function () {
    };

    alert("Starting websocket chat");
}

/*
 * Event for receiving a message.
 * 
 * @see function connectToChatserver() { wsocket.onmessage = onMessageReceived;
 */
function onMessageReceived(evt) {

    // native API
    var msg = JSON.parse(evt.data);

    var $messageLine = $('<tr><td class="received">' + msg.received + '</td><td class="nickname">' + msg.sender + '</td><td class="text">'
            + msg.message + '</td></tr>');
    var users = msg.nicknames;

    if (msg.message === 'user_name_already_in_use') {
        if (msg.sender === uid) {
            // get localized message
            alert(zk('$errLbl_UsernameAlreadyInUse').$().getValue());
           
            // close WebSocket for the double username
            wsocket.close();

            return;
        } else {
            // Don't show this message to other users
            return;
        }
    }

    // append the table row with the message data
    $('#response').append($messageLine);

    // clear the user list
    clearUserList();

    // add user names to table
    if (users !== null && users.length !== 0) {
        for (i = 0; i < users.length; ++i) {
            // console.log(users[i]);
            $('#users').append('<tr><td>' + users[i] + '</td></tr>');
        }
    }

    // scroll message table to last row
    scrollTable();
}

/*
 * Send message.
 */
function sendMessage() {

    // send only if text exists
    var str = zk('$txtMessage').$().getValue();
    if (str.length === 0) {
        return;
    }

    var msg = '{"message":"' + zk('$txtMessage').$().getValue() + '", "sender":"' + uid + '", "received":""}';

    // add by maikel
    console.log(msg);

    // send the message
    try {
        wsocket.send(msg);
    } catch (err) {
        console.log(err.message);
        alert(err);
        return;
    }

    // clear textbox
    zk('$txtMessage').$().setValue('');

    // scroll table to last row
    scrollTable();
}

/*
 * Notify others that user left the room.
 */
function sendLeaveMessage() {
    var user = '<b>' + zk('$txtb_nickname').$().getValue() + '</b>';
    var msg = '{"message":"' + user + ' has left the room.' + '", "sender":"' + zk('$txtb_nickname').$().getValue() + '", "received":""}';

    // send the message
    try {
        wsocket.send(msg);
    } catch (err) {
        console.log(err.message);
        alert(err);
    }

    // scroll table to last row
    scrollTable();
}

/*
 * Notify others that the user has left the room and clears the messages.
 */
function leaveRoom() {
    // close WebSocket
    closeWebSocket();


    // clear message list
    $('#response').empty();

    // clear the user list
    clearUserList();
}

/*
 * Close the WebSocket connection.
 */
function closeWebSocket() {
    wsocket.close();
}

/*
 * If document is ready loaded.
 */
$(document).ready(function () {
    connectToChatserver();

});

function fok() {
    zk.Widget.$(jq('$txtMessage')[0]).focus();
}

/*
 * Scroll table to last entry.
 */
function scrollTable() {

    var rowpos = $('#response tr:last').position();

    // check if it's the first entry
    if (typeof rowpos !== 'undefined') {
        if (typeof rowpos.top !== 'undefined') {
            jq('.divTable').scrollTop(rowpos.top);
        }
    }
}

/*
 * Deletes all user names from table.
 */
function clearUserList() {
    // delete all user names
    $('#users tbody tr').remove();
}




