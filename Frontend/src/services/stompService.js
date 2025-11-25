// STOMP client wrapper using @stomp/stompjs and sockjs-client fallback
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'


let client = null
let connected = false


const stompService = {
connect({ url, onConnect, onStompError, useSockJS = true }) {
if (client) {
console.warn('STOMP client already exists')
return
}


const brokerURL = url


client = new Client({
// If using SockJS set brokerURL to undefined and provide webSocketFactory
brokerURL: useSockJS ? undefined : brokerURL,
webSocketFactory: useSockJS ? () => new SockJS(brokerURL) : undefined,
connectHeaders: {},
debug: function (str) {
// console.log(str)
},
onConnect: (frame) => {
connected = true
onConnect && onConnect(frame)
},
onStompError: (frame) => {
onStompError && onStompError(frame)
},
// heartbeats
heartbeatIncoming: 10000,
heartbeatOutgoing: 10000,
})


client.activate()
},


disconnect() {
if (!client) return
client.deactivate()
client = null
connected = false
},


subscribe(destination, callback, headers = {}) {
if (!client) throw new Error('STOMP client not connected')
return client.subscribe(destination, (message) => {
let body = null
try { body = JSON.parse(message.body) } catch (e) { body = message.body }
callback(body, message)
}, headers)
},


send(destination, body = {}, headers = {}) {
if (!client) throw new Error('STOMP client not connected')
client.publish({ destination, body: JSON.stringify(body), headers })
},


isConnected() { return connected }
}


export default stompService