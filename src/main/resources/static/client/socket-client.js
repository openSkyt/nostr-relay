function socketClient(url) {
    const socket = new WebSocket(url);

    function status() {
        return socket.readyState;
    }

    function send(message) {
        socket.send(message);
    }

    function disconnect() {
        socket.close();
    }

    function onOpen(callback) {
        socket.addEventListener('open', function (event) {
            callback(event);
        });
    }

    function onMessage(callback) {
        socket.addEventListener('message', function (event) {
            callback(event);
        });
    }

    function onError(callback) {
        socket.addEventListener('error', function (event) {
            callback(event);
        });
    }

    function onClose(callback) {
        socket.addEventListener('close', function (event) {
            callback(event);
        });
    }

    return {
        status,
        send,
        disconnect,
        onOpen,
        onMessage,
        onError,
        onClose
    }
}