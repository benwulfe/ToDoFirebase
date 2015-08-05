package com.example.benwu.myapplication.nagger;

import com.firebase.client.Firebase;
import com.firebase.client.RunLoop;
import com.firebase.client.core.Context;
import com.firebase.client.core.JvmPlatform;
import com.firebase.client.utilities.DefaultRunLoop;
import com.firebase.client.utilities.LogWrapper;
import com.google.appengine.api.ThreadManager;

import java.util.concurrent.ThreadFactory;

public class AppEngineFirebasePlatform extends JvmPlatform {

    public AppEngineFirebasePlatform() {
        super(DefaultThreadFactory);
    }

    public static final ThreadFactory DefaultThreadFactory = new FirebaseThreadFactory();

    private static class FirebaseThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return ThreadManager.createBackgroundThread(r);
        }
    }

    @Override
    protected void initializeThread(Thread t, boolean setDaemon, String name) {
    }

    @Override
    public RunLoop newRunLoop(Context context) {
        final LogWrapper logger = context.getLogger("RunLoop");
        return new DefaultRunLoop(DefaultThreadFactory) {
            @Override
            public void handleException(Throwable e) {
                logger.error("Uncaught exception in Firebase runloop (" + Firebase.getSdkVersion() +
                        "). Please report to support@firebase.com", e);
            }
        };
    }

    @Override
    public String getPlatformVersion() {
        return "gae-" + Firebase.getSdkVersion();
    }
}
