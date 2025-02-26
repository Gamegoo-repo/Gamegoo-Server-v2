package com.gamegoo.gamegoo_v2.notification.repository;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.gamegoo.gamegoo_v2.notification.domain.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Notification> findNotificationsByCursor(Long memberId, Long cursor, int pageSize) {
        List<Notification> result = queryFactory.selectFrom(notification)
                .where(
                        notification.member.id.eq(memberId),
                        idBefore(cursor)
                )
                .orderBy(notification.createdAt.desc())
                .limit(pageSize + 1) // 다음 페이지가 있는지 확인하기 위해 +1
                .fetch();

        boolean hasNext = result.size() > pageSize;
        if (hasNext) {
            result.remove(result.size() - 1); // 다음 페이지가 있으면 마지막 요소를 제거
        }

        return new SliceImpl<>(result, Pageable.unpaged(), hasNext);
    }

    //--- BooleanExpression ---//

    private BooleanExpression idBefore(Long cursor) {
        return cursor != null ? notification.id.lt(cursor) : null;
    }

}
