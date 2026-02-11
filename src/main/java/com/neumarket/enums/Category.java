package com.neumarket.enums;

public enum Category {
  FURNITURE("Furniture"),
  ELECTRONICS("Electronics"),
  TEXTBOOKS("Textbooks"),
  CLOTHING("Clothing"),
  BIKES("Bikes & Scooters"),
  KITCHEN("Kitchen & Appliances"),
  FREE_STUFF("Free Stuff"),
  OTHER("Other");

  private final String displayName;

  Category(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}