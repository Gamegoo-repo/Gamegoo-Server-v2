package com.gamegoo.gamegoo_v2.content.report.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ReportPath path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board sourceBoard;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportTypeMapping> reportTypeMappingList = new ArrayList<>();

    public static Report create(Member fromMember, Member toMember, String content, ReportPath path,
                                Board sourceBoard) {
        return Report.builder()
                .content(content)
                .fromMember(fromMember)
                .toMember(toMember)
                .path(path)
                .sourceBoard(sourceBoard)
                .build();
    }

    @Builder
    private Report(String content, Member fromMember, Member toMember, ReportPath path, Board sourceBoard) {
        this.content = content;
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.path = path;
        this.sourceBoard = sourceBoard;
    }

}
