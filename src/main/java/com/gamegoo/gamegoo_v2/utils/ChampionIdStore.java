package com.gamegoo.gamegoo_v2.utils;

import java.util.Set;

public class ChampionIdStore {
    private static final Set<Long> CHAMPION_IDS = Set.of(
            1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L,
            22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L,
            42L, 43L, 44L, 45L, 48L, 50L, 51L, 53L, 54L, 55L, 56L, 57L, 58L, 59L, 60L, 61L, 62L, 63L, 64L, 67L,
            68L, 69L, 72L, 74L, 75L, 76L, 77L, 78L, 79L, 80L, 81L, 82L, 83L, 84L, 85L, 86L, 89L, 90L, 91L, 92L,
            96L, 98L, 99L, 101L, 102L, 103L, 104L, 105L, 106L, 107L, 110L, 111L, 112L, 113L, 114L, 115L, 117L,
            119L, 120L, 121L, 122L, 126L, 127L, 131L, 133L, 134L, 136L, 141L, 142L, 143L, 145L, 147L, 150L, 154L,
            157L, 161L, 163L, 164L, 166L, 200L, 201L, 202L, 203L, 221L, 222L, 223L, 233L, 234L, 235L, 236L, 238L,
            240L, 245L, 246L, 254L, 266L, 267L, 268L, 350L, 360L, 412L, 420L, 421L, 427L, 429L, 432L, 497L, 498L,
            516L, 517L, 518L, 523L, 526L, 555L, 711L, 777L, 875L, 876L, 887L, 888L, 895L, 897L, 901L, 902L, 910L,
            950L
    );

    public static boolean contains(Long championId) {
        return CHAMPION_IDS.contains(championId);
    }
}
