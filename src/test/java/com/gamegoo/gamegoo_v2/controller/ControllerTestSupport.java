package com.gamegoo.gamegoo_v2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.account.auth.annotation.resolver.AuthMemberArgumentResolver;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.security.CustomUserDetailsService;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.core.log.LogUtil;
import com.gamegoo.gamegoo_v2.core.log.LoggingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public abstract class ControllerTestSupport {

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthMemberArgumentResolver authMemberArgumentResolver;

    @MockitoBean
    protected JwtProvider jwtProvider;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    protected LoggingFilter loggingFilter;

    @MockitoBean
    protected LogUtil logUtil;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Member mockMember;

    protected static final String MOCK_EMAIL = "test@gmail.com";
    protected static final String MOCK_PASSWORD = "mockpassword";
    protected static final int MOCK_PROFILE_IMG = 1;
    protected static final LoginType MOCK_LOGIN_TYPE = LoginType.GENERAL;
    protected static final String MOCK_GAMENAME = "gamename";
    protected static final String MOCK_TAG = "KR1";
    protected static final Tier MOCK_TIER = Tier.BRONZE;
    protected static final int MOCK_GAME_RANK = 1;
    protected static final double MOCK_WIN_RATE = 50.0;
    protected static final int MOCK_GAME_COUNT = 10;
    protected static final boolean MOCK_IS_AGREE = true;

    @BeforeEach
    public void setUp() throws Exception {
        // csrf 설정
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(post("/**").with(csrf()))
                .defaultRequest(patch("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();

        // authMemberArgumentResolver가 mockMember를 반환하도록 Mock 설정
        mockMember = Member.builder()
                .email(MOCK_EMAIL)
                .password(MOCK_PASSWORD)
                .profileImage(MOCK_PROFILE_IMG)
                .loginType(MOCK_LOGIN_TYPE)
                .gameName(MOCK_GAMENAME)
                .tag(MOCK_TAG)
                .tier(MOCK_TIER)
                .gameRank(MOCK_GAME_RANK)
                .winRate(MOCK_WIN_RATE)
                .gameCount(MOCK_GAME_COUNT)
                .isAgree(MOCK_IS_AGREE)
                .build();
        given(authMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(mockMember);
    }

}
