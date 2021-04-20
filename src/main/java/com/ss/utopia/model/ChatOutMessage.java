package com.ss.utopia.model;

import java.util.Date;

public class ChatOutMessage {
  private String senderId;
  private String content;
  private String groupName;
  private String senderShortName;
  private Date sentTimestamp;

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderShortName() {
    return senderShortName;
  }

  public void setSenderShortName(String senderShortName) {
    this.senderShortName = senderShortName;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Date getSentTimestamp() {
    return sentTimestamp;
  }

  public void setSentTimestamp(Date sentTimestamp) {
    this.sentTimestamp = sentTimestamp;
  }
}
