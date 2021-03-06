kurento-room-client-android
=================
This repository contains Kurento Room API for Android.

This project is part of [NUBOMEDIA](http://www.nubomedia.eu).

The source code is available at [https://github.com/nubomedia-vtt/kurento-room-client-android](https://github.com/nubomedia-vtt/kurento-room-client-android).

Documentation
--------------------

Documentation available at: http://kurento-room-client-android.readthedocs.org/en/latest/

Repository structure
--------------------
This repository consists of an Android Studio library project with gradle build scripts. 

Usage
--------
The more detailed Developers Guide and Installation Guide are available at [http://kurento-room-client-android.readthedocs.org/en/latest/](http://kurento-room-client-android.readthedocs.org/en/latest/)

You can import this project to your own Android Studio project via Maven (jCenter or Maven Central) by adding the following line to module's `build.gradle` file:
```
compile 'fi.vtt.nubomedia:kurento-room-client-android:1.0.6'
```

If you want to build the project from source, you need to import the third-party libraries via Maven by adding the following lines to
the module's `build.gradle` file
```
compile 'fi.vtt.nubomedia:utilities-android:1.0.0'
compile 'fi.vtt.nubomedia:jsonrpc-ws-android:1.0.4'
compile 'fi.vtt.nubomedia:webrtcpeer-android:1.0.0'
```

Android application code
------------------------
```
import fi.vtt.nubomedia.kurentoroomclientandroid.KurentoRoomAPI;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomError;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomListener;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomNotification;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomResponse;
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor;

String wsRoomUri = "ws://YOUR_IP_ADDRESS:8080/room";

LooperExecutor executor = new LooperExecutor();
executor.requestStart();
RoomListener myRoomListener = ...
public int requestIndex = 0;

KurentoRoomAPI roomApi = new KurentoRoomAPI(executor, wsRoomUri, myRoomListener);
roomApi.connectWebSocket();
if(roomApi.isWebSocketConnected()){
 roomApi.sendJoinRoom("My Name", "My Room", requestIndex++);
 roomApi.sendMessage("My Room", "My Name", "My message.", requestIndex++);
 roomApi.disconnectWebSocket();
}


```

```
class MyRoomListener implements RoomListener(){
     @Override
    public void onRoomResponse(RoomResponse response) {
         String responseId = response.getId(); 
         String sessionId = response.getSessionId();
         HashMap<String><String> values = response.getValues();  
    }

    @Override
    public void onRoomError(RoomError error) {
        String errorCode = error.getCode();
        String errorData = error.getData();
    }

    @Override
    public void onRoomNotification(RoomNotification notification) {
      if(notification.getMethod()
        .equals(RoomListener.METHOD_PARTICIPANT_JOINED) {
        // TODO        
      } else if(notification.getMethod()
        .equals(RoomListener.METHOD_SEND_MESSAGE)) {
        // TODO
      } else ...
    }
}
```


Licensing
---------
[BSD](https://github.com/nubomedia-vtt/kurento-room-client-android/blob/master/LICENSE)

***Contribution policy***

You can contribute to this project through bug-reports, bug-fixes, new code or new documentation. For contributing to the project, drop a post to the mailing list providing information about your contribution and its value. In your contributions, you must comply with the following guidelines

•	You must specify the specific contents of your contribution either through a detailed bug description, through a pull-request or through a patch.

•	You must specify the licensing restrictions of the code you contribute.

•	For newly created code to be incorporated in the code-base, you must accept the code copyright, so that its open source nature is guaranteed.

•	You must justify appropriately the need and value of your contribution. There is no obligations in relation to accepting contributions from third parties.

Support
-------
Support is provided through the [NUBOMEDIA VTT Public Mailing List](https://groups.google.com/forum/#!forum/nubomedia-vtt).

