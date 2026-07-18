package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Placement cell profile — the recruitment authority for a {@link University}. Reviews and
 * approves/rejects jobs targeting its students. Backed 1:1 by a {@link User} with role
 * {@code PLACEMENT_CELL}.
 */
@Entity
@Table(name = "placement_cells",
        uniqueConstraints = @UniqueConstraint(name = "uk_placement_cells_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_placement_cells_university", columnList = "university_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementCell extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_placement_cells_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_placement_cells_university"))
    private University university;

    @Column(name = "office_name", nullable = false, length = 150)
    private String officeName;

    @Column(name = "contact_email", length = 180)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.APPROVED;
}
