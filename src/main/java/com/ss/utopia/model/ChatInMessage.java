package com.ss.utopia.model;

import java.util.Date;

public class ChatInMessage {
  private String senderUsername;
  private String senderId;
  private String senderNickname;
  private String message;
  private Date timestamp;
  private String receiverUsername;

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public void setSenderUsername(String senderUsername) {
    this.senderUsername = senderUsername;
  }

  public String getSenderNickname() {
    return senderNickname;
  }

  public void setSenderNickname(String senderNickname) {
    this.senderNickname = senderNickname;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getReceiverUsername() {
    return receiverUsername;
  }

  public void setReceiverUsername(String receiverUsername) {
    this.receiverUsername = receiverUsername;
  }
}
