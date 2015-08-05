/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.benwu.myapplication.nagger;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.Context;
import com.firebase.tubesock.WebSocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NagEmailer extends HttpServlet {

    @Override
    public void init() throws ServletException {
        if (!(Context.platform instanceof AppEngineFirebasePlatform)) {
            Context.platform = new AppEngineFirebasePlatform();
            WebSocket.DefaultFactory = AppEngineFirebasePlatform.DefaultThreadFactory;
        }

        Firebase firebase = new Firebase("https://incandescent-torch-2575.firebaseio.com/foobar");
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final StringBuilder newItemMessage = new StringBuilder();

                newItemMessage.append("New TODO:\n");
                for (DataSnapshot field : dataSnapshot.getChildren()) {
                    newItemMessage.append(field.getKey())
                            .append(":")
                            .append(field.getValue().toString())
                            .append("\n");
                }

                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);

                try {
                    Message msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress("nagger@benwu-test1.appspotmail.com", "Todo Nagger"));
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress("benwu@google.com", "Mr. Wulfe"));
                    msg.setSubject("You have TODO Items!");
                    msg.setText(newItemMessage.toString());
                    Transport.send(msg);

                } catch (MessagingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
