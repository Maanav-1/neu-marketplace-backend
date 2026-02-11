package com.neumarket.model;

import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Condition condition;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ListingStatus status = ListingStatus.ACTIVE;

  @Column(unique = true, nullable = false)
  private String slug;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private LocalDateTime expiresAt;

  // Relationships
  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("displayOrder ASC")
  @Builder.Default
  private List<ListingImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Conversation> conversations = new ArrayList<>();

  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<SavedItem> savedBy = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (expiresAt == null) {
      expiresAt = LocalDateTime.now().plusDays(30);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Helper method to add image
  public void addImage(ListingImage image) {
    images.add(image);
    image.setListing(this);
  }

  public void removeImage(ListingImage image) {
    images.remove(image);
    image.setListing(null);
  }
}