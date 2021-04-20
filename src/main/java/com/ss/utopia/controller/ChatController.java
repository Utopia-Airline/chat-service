package com.ss.utopia.controller;

import com.ss.utopia.model.ChatInMessage;
import com.ss.utopia.model.ChatOutMessage;
import com.ss.utopia.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ChatController {
  private static Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
  private final SimpMessagingTemplate simpMessagingTemplate;

  private final ChatService chatService;

  @Autowired
  public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.chatService = chatService;
  }

  @MessageMapping("/secured/room/chat")
  public void handleAdminChat(
    @Payload ChatInMessage message,
    Principal user,
    Authentication auth,
    @Header("simpSessionId") String sessionId
  ) throws Exception {
    try {
      if (null != user)
        LOGGER.info("Chat {} has sent the message {}", user.getName(), message.getMessage());
      else
        LOGGER.info("Chat Guest has sent the message {}", message.getMessage());
      LOGGER.info("the session is {}", sessionId);
      var chatOutMessage = new ChatOutMessage();
      chatOutMessage.setContent(message.getMessage());
      chatOutMessage.setSenderId(message.getSenderId());
      if (null != user) {
        chatOutMessage.setSenderShortName(user.getName());
      } else {
        chatOutMessage.setSenderShortName("Guest #" + sessionId);
      }
      String customerId = (isAdmin(auth)) ? message.getReceiverUsername() : message.getSenderId();
      if (chatService.chats.containsKey(customerId)) {
        List messages = chatService.chats.get(customerId);
        messages.add(chatOutMessage);
      }
      simpMessagingTemplate.convertAndSendToUser(message.getReceiverUsername(), "/queue/private/chat", chatOutMessage);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  @MessageMapping("/secured/room/chat/history")
  public void handleAdminLoadChatHistory(
    @Payload ChatInMessage message,
    Authentication auth
  ) throws Exception {
    try {
      String customerId = "";
      if (isAdmin(auth)) {
        customerId = message.getReceiverUsername();
      } else {
        customerId = message.getSenderId();
      }
      chatService.chats.get(customerId).stream().forEach(chat -> {
        simpMessagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/private/load", chat);
      });
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  @MessageMapping("/secured/room/join/history")
  public void handleAdminLoadJoinHistory(
    @Payload ChatInMessage message,
    Authentication auth
  ) throws Exception {
    try {
      String customerId = "";
      if (isAdmin(auth)) {
        customerId = message.getReceiverUsername();
        chatService.chats.keySet().forEach(id -> {
          ChatOutMessage customer = new ChatOutMessage();
          customer.setSenderId(id);
          if (id.matches("^Guest.*$"))
            customer.setSenderShortName("Guest #" + id.substring(5));
          else
            customer.setSenderShortName(id);
          simpMessagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/private/join", customer);
        });
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private boolean isAdmin(Authentication auth) {
    try {
      return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    } catch (Exception e) {
      return false;
    }
  }

  @MessageMapping("/secured/room/update")
  public void handleAdminChatUserIsTyping(ChatInMessage message) {
    try {
      LOGGER.info("Handle user typing...");
      var chatOutMessage = new ChatOutMessage();
      chatOutMessage.setContent(message.getMessage());
      simpMessagingTemplate.convertAndSendToUser(message.getReceiverUsername(), "/queue/private/update", chatOutMessage);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  @MessageMapping("/secured/room/join")
  public void handleAdminChatUserJoins(ChatInMessage message,
                                       Principal user,
                                       Authentication auth,
                                       @Header("simpSessionId") String sessionId) {
    try {
      LOGGER.info("Handle user Joining");
      var chatOutMessage = new ChatOutMessage();
      chatOutMessage.setSenderId(message.getSenderId());
      chatOutMessage.setContent(message.getMessage());
      if (null != user) {
        chatOutMessage.setSenderShortName(user.getName());
      } else {
        chatOutMessage.setSenderShortName("Guest #" + sessionId);
      }
      String customerId = (isAdmin(auth)) ? message.getReceiverUsername() : message.getSenderId();
      if (!chatService.chats.containsKey(customerId)) {
        chatService.chats.putIfAbsent(customerId, new ArrayList<>());
        ChatOutMessage botMessage = new ChatOutMessage();
        botMessage.setSenderId("admin");
        botMessage.setContent(
          "Hi! I’m Utopia’s Virtual Assistant. I can help you find answers to common questions.\n" +
            "What can I help you with today?");
        botMessage.setSenderShortName("Virtual Bot");
        botMessage.setSentTimestamp(new Date());
        chatService.chats.get(customerId).add(botMessage);
        // simpMessagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/private/join", botMessage);
      }
      simpMessagingTemplate.convertAndSendToUser(message.getReceiverUsername(), "/queue/private/join", chatOutMessage);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  @MessageMapping("/secured/room/errors")
  public void handleAdminChatException(ChatInMessage message) {
    try {
      LOGGER.info("Handle Chat error");
      var chatOutMessage = new ChatOutMessage();
      chatOutMessage.setContent(message.getMessage());
      simpMessagingTemplate.convertAndSendToUser(message.getReceiverUsername(), "/queue/private/error", chatOutMessage);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }
}
