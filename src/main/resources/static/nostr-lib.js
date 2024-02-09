function nostrLib() {
    function getEventId(event) {
        const eventData = JSON.stringify([
            0,                      // Reserved for future use
            event['pubkey'],        // The sender's public key
            event['created_at'],    // Unix timestamp
            event['kind'],          // Message “kind” or type
            event['tags'],          // Tags identify replies/recipients
            event['content']        // Your note contents
        ]);

        return bitcoinjs.crypto.sha256(eventData).toString('hex');
    }

    function sign(data, privateKey) {
        return nobleSecp256k1.schnorr.sign(data, privateKey);
    }

    function verifyEvent(event) {
        return nobleSecp256k1.schnorr.verify(event.id, event.sig, event.pubkey);
    }

    function getPublicKey(privateKey) {
        return nobleSecp256k1.schnorr.getPublicKey(privateKey);
    }

    function generateKeyPair() {
            return bitcoinjs.ECPair.makeRandom();
    }

    function generateNostrKeys() {
        const keypair = generateKeyPair();

        const privateKey = keypair.privateKey.toString("hex");
        const publicKey = keypair.publicKey.toString("hex").substring(2);

        return {
            privateKey,
            publicKey
        };
    }


    return {
        getEventId,
        sign,
        verifyEvent,
        getPublicKey,
        generateNostrKeys
    }

}