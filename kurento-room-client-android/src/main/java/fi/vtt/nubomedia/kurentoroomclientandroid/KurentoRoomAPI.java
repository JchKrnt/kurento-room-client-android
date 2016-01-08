package fi.vtt.nubomedia.kurentoroomclientandroid;

import android.util.Log;

import net.minidev.json.JSONObject;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;

import fi.vtt.nubomedia.jsonrpcwsandroid.JsonRpcNotification;
import fi.vtt.nubomedia.jsonrpcwsandroid.JsonRpcRequest;
import fi.vtt.nubomedia.jsonrpcwsandroid.JsonRpcResponse;
import fi.vtt.nubomedia.jsonrpcwsandroid.JsonRpcWebSocketClient;
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor;

/**
 * Class that handles all Room API calls and passes asynchronous
 * responses and notifications to a RoomListener interface.
 */
public class KurentoRoomAPI implements JsonRpcWebSocketClient.WebSocketConnectionEvents  {
    private static final String LOG_TAG = "KurentoRoomAPI";
    private JsonRpcWebSocketClient client = null;
    private LooperExecutor executor = null;
    private String wsUri = null;
    private RoomListener roomListener = null;

    /**
     * Constructor that initializes required instances and parameters for the API calls.
     * WebSocket connections are not established in the constructor. User is responsible
     * for opening, closing and checking if the connection is open through the corresponding
     * API calls.
     *
     * @param executor
     * @param uri
     * @param listener
     */
    public KurentoRoomAPI(LooperExecutor executor, String uri, RoomListener listener){
        super();
        this.executor = executor;
        this.wsUri = uri;
        this.roomListener = listener;
    }

    /**
     * Opens a web socket connection to the predefined URI as provided in the constructor.
     * The method responds immediately, whether or not the connection is opened.
     * The method isWebSocketConnected() should be called to ensure that the connection is open.
     */
    public void connectWebSocket() {
        try {
            if(isWebSocketConnected()){
                return;
            }
            URI uri = new URI(wsUri);
            client = new JsonRpcWebSocketClient(uri, this,executor);
            executor.execute(new Runnable() {
                public void run() {
                    client.connect();
                }
            });

        } catch (Exception exc){
            Log.e(LOG_TAG, "connectWebSocket", exc);
        }
    }

    /**
     * Method to check if the web socket connection is connected.
     *
     * @return true if the connection state is connected, and false otherwise.
     */
    public boolean isWebSocketConnected(){
        if(client!=null){
            return (client.getConnectionState().equals(JsonRpcWebSocketClient.WebSocketConnectionState.CONNECTED));
        } else {
            return false;
        }
    }

    /**
     * Attempts to close the web socket connection asynchronously.
     */
    public void disconnectWebSocket() {
        try {
            if(client!= null ) {
                executor.execute(new Runnable() {
                    public void run() {
                        client.disconnect(false);
                    }
                });
            }
        } catch (Exception exc){
            Log.e(LOG_TAG, "disconnectWebSocket", exc);
        } finally {
            ;
        }
    }

    /**
     * Method for the user to join the room. If the room does not exist previously,
     * it will be created.
     *
     * The response will contain the list of users and their streams in the room.
     *
     * @param userId is the username as it appears to all other users.
     * @param roomId is the name of the room to be joined.
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendJoinRoom(String userId, String roomId, int id){
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("user", userId);
        namedParameters.put("room", roomId);
        send("joinRoom", namedParameters, id);
    }

    /**
     * Method will leave the current room.
     *
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendLeaveRoom(int id){
        send("leaveRoom", null, id);
    }

    /**
     * Method to publish a video. The response will contain the sdpAnswer attribute.
     *
     * @param sdpOffer is a string sent by the client
     * @param doLoopback is a boolean value enabling media loopback
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendPublishVideo(String sdpOffer, boolean doLoopback, int id){
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("sdpOffer", sdpOffer);
        namedParameters.put("doLoopback", new Boolean(doLoopback));
        send("publishVideo", namedParameters, id);
    }

    /**
     * Method unpublishes a previously published video.
     *
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendUnpublishVideo(int id){
        send("unpublishVideo", null, id);
    }

    /**
     * Method represents the client's request to receive media from participants in
     * the room that published their media. The response will contain the sdpAnswer attribute.
     *
     * @param sender is a combination of publisher's name and his currently opened stream
     *               (usually webcam) separated by underscore. For example: userid_webcam
     * @param sdpOffer is the SDP offer sent by this client.
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendReceiveVideoFrom(String sender, String sdpOffer, int id){
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("sdpOffer", sdpOffer);
        namedParameters.put("sender", sender);
        send("receiveVideoFrom", namedParameters, id);
    }

    /**
     * Method represents a client's request to stop receiving media from a given publisher.
     * Response will contain the sdpAnswer attribute.
     *
     * @param userId is the publisher's username.
     * @param streamId is the name of the stream (typically webcam)
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendUnsubscribeFromVideo(String userId, String streamId, int id){
        String sender = userId+"_"+streamId;
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("sender", sender);
        send("unsubscribeFromVideo", namedParameters, id);
    }

    /**
     * Method carries the information about the ICE candidate gathered on the client side.
     * This information is required to implement the trickle ICE mechanism.
     *
     * @param endpointName is the username of the peer whose ICE candidate was found
     * @param candidate contains the candidate attribute information
     * @param sdpMid is the media stream identification, "audio" or "video", for the m-line,
     *               this candidate is associated with.
     * @param sdpMLineIndex is the index (starting at 0) of the m-line in the SDP,
     *                      this candidate is associated with.
     */
    public void sendOnIceCandidate(String endpointName, String candidate, String sdpMid, String sdpMLineIndex){
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("endPointName", endpointName);
        namedParameters.put("candidate", candidate);
        namedParameters.put("sdpMid", sdpMid);
        namedParameters.put("sdpMLineIndex", sdpMLineIndex);
        send("receiveVideoFrom", namedParameters, -1);
    }

    /**
     * Method sends a message from the user to all other participants in the room.
     *
     * @param roomId is the name of the room.
     * @param userId is the username of the user sending the message.
     * @param message is the text message sent to the room.
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendMessage(String roomId, String userId, String message, int id){
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("message", message);
        namedParameters.put("userMessage", userId);
        namedParameters.put("roomMessage", roomId);
        send("sendMessage", namedParameters, id);
    }

    /**
     * Method to send any custom requests that are not directly implemented by the Room server.
     *
     * @param names is an array of parameter names.
     * @param values is an array of parameter values where the index is corresponding with
     *               the applicable name value in the names array.
     * @param id is an index number to track the corresponding response message to this request.
     */
    public void sendCustomRequest(String[] names, String[] values, int id){
        if(names==null || values==null||names.length!=values.length){
            return;  // mismatching name-value pairs
        }
        HashMap<String, Object> namedParameters = new HashMap<String, Object>();
        for(int i=0;i<names.length;i++) {
            namedParameters.put(names[i], values[i]);
        }
        send("customRequest", namedParameters, id);

    }

    /**
     *
     * @param method
     * @param namedParameters
     * @param id
     */
    private void send(String method, HashMap<String, Object> namedParameters, int id){

        try {
            final JsonRpcRequest request = new JsonRpcRequest();
            request.setMethod(method);
            if(namedParameters!=null) {
                request.setNamedParams(namedParameters);
            }
            if(id>=0) {
                request.setId(new Integer(id));
            }
            executor.execute(new Runnable() {
                public void run() {
                    if(isWebSocketConnected()) {
                        client.sendRequest(request);
                    }
                }
            });
        } catch (Exception exc){
            Log.e(LOG_TAG, "send: "+method, exc);
        }
    }



    /* WEB SOCKET CONNECTION EVENTS */

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(LOG_TAG, "onOpen: "+handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onRequest(JsonRpcRequest request) {
        Log.d(LOG_TAG, "onRequest: "+request.toString());
    }


    /**
     * Callback method that relays the RoomResponse or RoomError to the RoomListener interface.
     */
    @Override
    public void onResponse(JsonRpcResponse response) {
        if(response.isSuccessful()){
            JSONObject jsonObject = (JSONObject)response.getResult();
            RoomResponse roomResponse = new RoomResponse(response.getId().toString(), jsonObject);
            roomListener.onRoomResponse(roomResponse);
        } else {
            RoomError roomError = new RoomError(response.getError());
            roomListener.onRoomError(roomError);
        }
    }

    /**
     * Callback method that relays the RoomNotification to the RoomListener interface.
     */
    @Override
    public void onNotification(JsonRpcNotification notification) {
        RoomNotification roomNotification = new RoomNotification(notification);
        roomListener.onRoomNotification(roomNotification);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(LOG_TAG, "onClose: "+reason+";"+code+";"+remote);
    }

    @Override
    public void onError(Exception e) {
        Log.e(LOG_TAG, "onError: "+e.getMessage(), e);
    }

}