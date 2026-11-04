package com.setmon.core.model

/**
 * core:model — 앱 전체가 같이 쓰는 데이터 클래스를 모아두는 곳.
 * 1주차 목요일 Swagger 확정 후 서버 응답 모양에 맞춰 채운다.
 */
data class User(
    val id: Long,
    val nickname: String,
)

/** 서버 연결 전까지 쓰는 가짜 "내 계정" id. 로그인 API가 붙으면 지운다. */
const val MOCK_MY_USER_ID: Long = 1L
