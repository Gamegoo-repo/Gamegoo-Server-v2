package com.gamegoo.gamegoo_v2.matching.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingResult extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_result_id")
    private Long id;

    @Column(nullable = false)
    private MatchingStatus status;

    @Column
    private Boolean mannerMessageSent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_uuid", insertable = false, updatable = false)
    private MatchingRequest matchingRequestSender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_uuid", insertable = false, updatable = false)
    private MatchingRequest matchingRequestReceiver;


    // MatchingResult 생성 메서드
    public static MatchingResult create(MatchingStatus status, Boolean mannerMessageSent,
                                        MatchingRequest matchingRequestSender,
                                        MatchingRequest matchingRequestReceiver) {
        return MatchingResult.builder()
                .status(status)
                .mannerMessageSent(mannerMessageSent)
                .matchingRequestSender(matchingRequestSender)
                .matchingRequestReceiver(matchingRequestReceiver)
                .build();
    }

    // MatchingResult Builder
    @Builder
    private MatchingResult(MatchingStatus status, Boolean mannerMessageSent,
                           MatchingRequest matchingRequestSender,
                           MatchingRequest matchingRequestReceiver) {
        this.status = status;
        this.mannerMessageSent = mannerMessageSent;
        this.matchingRequestSender = matchingRequestSender;
        this.matchingRequestReceiver = matchingRequestReceiver;
    }

}
