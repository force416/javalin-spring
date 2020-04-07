<template>
    <div>
        <h1>Home</h1>
        <div>Hello World!!</div>
    </div>
</template>
<script>
    let socket = null;
    module.exports = {
        created() {
            socket = this.initWs();
        },
        methods: {
            initWs() {
                const socket = new WebSocket("ws://" + location.host + "/chat");

                socket.onopen = () => {
                    console.log("Connection is open...");
                };

                socket.onmessage = (evt) => {
                    const received_msg = evt.data;
                    console.log('Message is received...' + received_msg);
                };

                socket.onclose = () => {
                    console.log("Connection is closed...");
                };

                return socket;
            },
        },
    };
</script>
<style scoped>

</style>

