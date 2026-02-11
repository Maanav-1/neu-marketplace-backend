package com.neumarket.enums;

public enum ReportReason {
  SPAM("Spam or misleading"),
  INAPPROPRIATE("Inappropriate content"),
  PROHIBITED_ITEM("Prohibited item"),
  SCAM("Suspected scam"),
  HARASSMENT("Harassment or abuse"),
  FAKE_LISTING("Fake or fraudulent listing"),
  WRONG_CATEGORY("Wrong category"),
  OTHER("Other");

  private final String displayName;

  ReportReason(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}