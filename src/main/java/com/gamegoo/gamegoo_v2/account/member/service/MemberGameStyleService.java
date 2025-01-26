package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberGameStyle;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberGameStyleRepository;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import com.gamegoo.gamegoo_v2.game.repository.GameStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberGameStyleService {

    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;

    /**
     * @param member          회원
     * @param gameStyleIdList 수정할 게임 스타일 리스트
     */
    @Transactional
    public void updateGameStyle(Member member, List<Long> gameStyleIdList) {
        // request의 Gamestyle 조회
        List<GameStyle> requestGameStyleList = findRequestGameStyle(gameStyleIdList);

        // 현재 DB의 GameStyle 조회
        List<MemberGameStyle> currentMemberGameStyleList = findCurrentMemberGameStyleList(member);

        // request에 없고, DB에 있는 GameStyle 삭제
        removeUnnecessaryGameStyles(member, requestGameStyleList, currentMemberGameStyleList);

        // request에 있고, DB에 없는 GameStyle 추가
        addNewGameStyles(member, requestGameStyleList, currentMemberGameStyleList);
    }

    /**
     * request id로 GameStyle Entity 조회
     *
     * @return request의 GamestyleList
     */
    private List<GameStyle> findRequestGameStyle(List<Long> gameStyleIdList) {
        return gameStyleIdList.stream()
                .map(id -> gameStyleRepository.findById(id).orElseThrow(() -> new MemberException(ErrorCode.GAMESTYLE_NOT_FOUND)))
                .toList();
    }

    /**
     * 현재 DB의 MemberGameStyle List 조회
     *
     * @return MemberGameStyleList
     */
    private List<MemberGameStyle> findCurrentMemberGameStyleList(Member member) {
        return new ArrayList<>(member.getMemberGameStyleList());
    }

    /**
     * 불필요한 GameStyle 제거
     *
     * @param member                  회원
     * @param requestedGameStyles     새로운 GameStyle
     * @param currentMemberGameStyles 현재 Gamestyle
     */
    @Transactional
    protected void removeUnnecessaryGameStyles(Member member, List<GameStyle> requestedGameStyles,
                                               List<MemberGameStyle> currentMemberGameStyles) {
        currentMemberGameStyles.stream()
                .filter(mgs -> !requestedGameStyles.contains(mgs.getGameStyle()))
                .forEach(mgs -> {
                    mgs.removeMember(member);
                    memberGameStyleRepository.delete(mgs);
                });
    }

    /**
     * 새로운 GameStyle 추가
     *
     * @param member                  사용자
     * @param requestedGameStyles     변경 후 게임스타일
     * @param currentMemberGameStyles 변경 전 게임스타일
     */
    @Transactional
    protected void addNewGameStyles(Member member, List<GameStyle> requestedGameStyles,
                                    List<MemberGameStyle> currentMemberGameStyles) {
        List<GameStyle> currentGameStyles = currentMemberGameStyles.stream()
                .map(MemberGameStyle::getGameStyle)
                .toList();

        requestedGameStyles.stream()
                .filter(gs -> !currentGameStyles.contains(gs))
                .forEach(gs -> {
                    MemberGameStyle newMemberGameStyle = MemberGameStyle.create(gs, member);
                    memberGameStyleRepository.save(newMemberGameStyle);
                });
    }


}
