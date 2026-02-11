package com.neumarket.enums;

public enum ReportStatus {
  PENDING,      // New report, not reviewed
  REVIEWED,     // Admin has seen it
  ACTION_TAKEN, // Content removed / user banned
  DISMISSED     // Report was invalid
}