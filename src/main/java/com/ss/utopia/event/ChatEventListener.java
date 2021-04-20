package com.ss.utopia.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class ChatEventListener {

  @EventListener
  private void handleSessionConnected(SessionConnectEvent event) {
    System.out.println("connected");
  }

  @EventListener
  private void handleSessionDisconnected(SessionDisconnectEvent event) {
    System.out.println("disconnected");
  }
}
