function nostrClient(url) {
    // url = 'ws://localhost:8080'; || 'wss://nostr-server.herokuapp.com'

    if (url === undefined || url.match(/^wss?:\/\//) === null) {
        throw new Error('Invalid URL');
    }

    const wsClient = socketClient(url);

    function onEvent(callback) {
        wsClient.onMessage(function (message) {
            const event = JSON.parse(message.data)[2]
            if (!event) return;

            callback(event);
        });
    }

    function signEvent(event, privateKey) {
        event.id = nostrLib.getEventId(event);
        event.sig = nostrLib.sign(event.id, privateKey);

        return event;
    }

    function publish(event) {
        if (event.id === undefined || event.sig === undefined) {
            throw new Error('Can\'t publish. Event must be signed, use signEvent()');
        }

        wsClient.send(JSON.stringify(["EVENT", event]));
    }

    function getRID() {
        return fetch(url.replace(/^ws/, "http"), {
            headers: {
                'Accept': 'application/nostr+json'
            },
        })
        .then(resp => resp.json());
    }

    return {
        getRID,
        signEvent,
        publish,
        onEvent,
        send : wsClient.send,
        onOpen : wsClient.onOpen,
        onError : wsClient.onError,
        onClose : wsClient.onClose,
    }
}