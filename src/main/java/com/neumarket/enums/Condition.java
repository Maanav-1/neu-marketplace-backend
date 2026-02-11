package com.neumarket.enums;

public enum Condition {
  NEW("New"),
  LIKE_NEW("Like New"),
  GOOD("Good"),
  FAIR("Fair"),
  POOR("Poor");

  private final String displayName;

  Condition(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}