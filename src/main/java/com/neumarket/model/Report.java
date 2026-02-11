package com.neumarket.model;

import com.neumarket.enums.ReportReason;
import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Who reported
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporter;

  // What type of content
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportType reportType;

  // ID of the reported content (listing_id, user_id, or message_id)
  @Column(nullable = false)
  private Long targetId;

  // Why reported
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportReason reason;

  // Additional details from reporter
  @Column(length = 1000)
  private String description;

  // Status of the report
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ReportStatus status = ReportStatus.PENDING;

  // Admin who reviewed
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by_id")
  private User reviewedBy;

  // Admin notes
  @Column(length = 1000)
  private String adminNotes;

  private LocalDateTime reviewedAt;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}