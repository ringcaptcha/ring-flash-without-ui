# ring-flash-without-ui

This sample shows how to implement RingCaptcha's Blink authentication with your own UI.  

## Getting Started

* Use gradle dependency ```compile 'com.ringcaptcha.android:ringcaptcha:x.x.x'```  
Please refer to [app/build.gradle](./app/build.gradle) for the version information.  

## Permissions

This SDK requests the following permissions:
```
Manifest.permission.READ_PHONE_STATE - dangerous
Manifest.permission.ANSWER_PHONE_CALLS - dangerous (needed for API 26 and higher)
```
There is no need to add these permissions to AndroidManifest file because it will be merged automatically.
If you think of adding Marshmallow(6) or higher Android versions as a target of your application, take care of granting all dangerous runtime permissions. https://developer.android.com/guide/topics/permissions/requesting.html this article could be useful.

**Warning:** You will not be able to use the SDK without these permissions!

## Using SDK features separately

Here is a code sample.  
[WaitActivity.java](./app/src/main/java/com/example/yuta/ringflashwithoutuisample/WaitActivity.java)  
This is an intent called from [MainActivity.java](./app/src/main/java/com/example/yuta/ringflashwithoutuisample/MainActivity.java)  

---

SDK provides an ability to use the next features separately:
1. Request a call from RC
2. Intercept cellular broadcast for specified period of time
3. Verify a call

To use these features, create ```Credentials``` instance providing *APP_KEY*, *SECRET_KEY* and phone number to call on.
```java
RingFlashCredentials credentials = new RingFlashCredentials();
credentials.setSecretKey("Replace with your SECRET_KEY");
credentials.setAppKey("Replace with your APP_KEY");
credentials.setPhoneNumber("Replace with user's phone number");
```

#### Request a call from RC
To request a call from RC, use ```RingFlashAPIController.requestVoiceCall()```:
```java
RingFlashAPIHandler ringFlashAPIHandler = new RingFlashAPIHandler() {
    @Override
    public void onSuccess(RingFlashResponse response) {
        boolean callRequested = response.checkStatus();
        if (callRequested) {
            //Actions needed to perform after call was requested
        } else {
            //Actions needed to perform after request was failed
        }
    }

    @Override
    public void onError(Exception exception) {
      //Display an error to a user
    }
};

RingFlashAPIController ringFlashAPIController = new RingFlashAPIController(getApplicationContext());
ringFlashAPIController.requestVoiceCall(credentials, ringFlashAPIHandler);
```

#### Intercept cellular broadcast for specified period of time
To start intercepting a cellular broadcast use the following snippet: 
```java
RingFlashInterceptionHandler interceptionHandler = new RingFlashInterceptionHandler() {
            @Override
            public void onCallEnded(String callerNumber) {
                //Callback execution is triggered after call is intercepted
                //At this point, the verification can be performed.
            }
        };

RingFlashSDK ringFlashSDK = RingFlashSDK.builder()
                .setContext(getApplicationContext())
                .setSecretKey("Replace with your SECRET_KEY")
                .setAppKey("Replace with your APP_KEY")
                .setHandler(getHandler())
                .setPhoneNumber("Replace with user's phone number")
                .build();
ringFlashSDK.startCellularBroadcastIntercepting(interceptionHandler);
```

Here a RingFlashSDK instance is created providing credentials followed by ```RingFlashVerificationHandler```.  

Needed actions can be performed after some important event has triggered a callback execution. These callbacks are provided by ```RingFlashVerificationHandler```. Check ``` getHandler()``` method used in example above. Some of the possible callbacks use cases are specified in comments.

```java
private RingFlashVerificationHandler getHandler() {
        return new RingFlashVerificationHandler() {

            @Override
            public void onFlashCallRequested(RingFlashResponse response) {
                //Redirect a user to Activity with notification like "We are calling you."
            }

            @Override
            public void onFlashCallRequestedFailed(RingFlashResponse response) {
                //Display an error to a user and handle this error(e.g. if it's ERROR_WAIT_TO_RETRY notify a user, that he can perform another attempt to verify only after a number of seconds specified in response object). 
            }

            @Override
            public void onVerificationInitiated() {
                //Callback execution is triggered after SDK intercepts a call and sends a verification request to RingCaptcha
                //Spinner which shows that verification is in process can be displayed to user until the execution of onVerified or onVerifiedFailed is not triggered
            }

            @Override
            public void onVerified() {
                //Notify a user that verification was successful
            }

            @Override
            public void onVerificationFailed(RingFlashResponse response) {
                //Display an error to a user and handle this error. 
            }

            @Override
            public void onError(Exception e) {
                //Display an error to the user
            }
        };
    }
```
The full list of errors is available at https://my.ringcaptcha.com/docs/api. To retrieve an error type, use `RingFlashResponse.getMessage()` method.

SDK will drop every incoming call and trigger ```onCallEnded(String callerNumber)``` method execution by passing caller's number as a parameter.  
This number is necessary to make the verification request later.  

To stop intercepting a cellular broadcast immediately:
```java
RingFlashSDK.stopCellularBroadcastIntercepting();
```
To schedule stop of cellular broadcast intercepting two methods can be used:
```java
RingFlashSDK.scheduleStopOfCellularBroadcastIntercepting(int seconds);
```
or
```java
RingFlashSDK.scheduleStopOfCellularBroadcastIntercepting();
```
In the first case, interception of a cellular broadcast is timed out after seconds value passed as a parameter.  
In the second case, SDK stops intercepting of a cellular broadcast after 60 seconds.

#### Verify a call
To verify a call from RC, use ```RingFlashAPIController.requestVerification()```:
```java
RingFlashAPIHandler ringFlashAPIHandler = new RingFlashAPIHandler() {
    @Override
    public void onSuccess(RingFlashResponse response) {
        boolean is_verified = response.checkStatus();
        if (is_verified) {
            //Actions needed to perform after verification
        } else {
            //Actions needed to perform if verification failed
        }
    }

    @Override
    public void onError(Exception exception) {
      //Display an error to a user
    }
};

RingFlashAPIController ringFlashAPIController = new RingFlashAPIController(getApplicationContext());
ringFlashAPIController.requestVerification(credentials, "Replace with caller number", ringFlashAPIHandler);
```
**Warning:** The same Credentials object must be passed to both **requestVoiceCall** and **requestVerification** methods. In another case, verification fails even if Credentials instances have the same APP_KEY, SECRET_KEY, and phone number.

#### Parse response from RingCaptcha
To parse response from RingCaptcha, use ```RingFlashResponse``` class. Following getters exists:
```java
public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPhone() {
        return phone;
    }

    public String getService() {
        return service;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public int getRetry_in() {
        return retry_in;
    }
```

You can access them easily in `onSuccess` method:

```java
RingFlashAPIHandler ringFlashAPIHandler = new RingFlashAPIHandler() {
    @Override
    public void onSuccess(RingFlashResponse response) {
        boolean is_verified = response.checkStatus();
        if (is_verified) {
            // Get verified phone number and session token from the response object:
            Log.i(MainActivity.TAG, response.getPhone());
            Log.i(MainActivity.TAG, response.getToken());
        } else {
            //Actions needed to perform after request was failed
        }
    }
}
```

### Retrieving an additional info from callbacks
Almost all callbacks provided by ```RingFlashAPIHandler``` and ```RingFlashVerificationHandler``` has a ```RingFlashResponse``` object as a parameter. This object contains all data from RC API response, i.e., seconds needed to wait before another attempt, transaction id, token etc.
