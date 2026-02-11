package com.neumarket.repository;

import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import com.neumarket.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

  // Get all reports with filters
  Page<Report> findByStatus(ReportStatus status, Pageable pageable);

  Page<Report> findByReportType(ReportType reportType, Pageable pageable);

  Page<Report> findByStatusAndReportType(ReportStatus status, ReportType reportType, Pageable pageable);



  // Get pending reports
  List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);

  // Count reports by status - Spring Data JPA provides implementation automatically
  long countByStatus(ReportStatus status);

  // Check if user already reported this content
  boolean existsByReporterIdAndReportTypeAndTargetId(Long reporterId, ReportType reportType, Long targetId);

  // Get reports for specific content
  List<Report> findByReportTypeAndTargetId(ReportType reportType, Long targetId);

  // Count reports for specific content
  long countByReportTypeAndTargetId(ReportType reportType, Long targetId);

  // Stats query for grouping
  @Query("SELECT r.reportType, COUNT(r) FROM Report r GROUP BY r.reportType")
  List<Object[]> countByReportType();
}