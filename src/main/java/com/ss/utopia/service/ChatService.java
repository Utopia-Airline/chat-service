package com.ss.utopia.service;

import com.ss.utopia.model.ChatOutMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
  public final Map<String, List<ChatOutMessage>> chats = new HashMap<>();
}
