package com.gamegoo.gamegoo_v2.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockMemberSecurityContextFactory.class)
public @interface WithCustomMockMember {

    Long memberId = 1L;

}
