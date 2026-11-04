package com.setmon.core.data

/*
 * core:data — Repository가 들어갈 곳.
 *
 * 화면(feature)은 Repository "인터페이스"만 보고, 실제 데이터를 서버에서 가져올지(Retrofit)
 * 가짜 데이터(FakeRepository)를 줄지는 이 모듈의 Hilt 설정이 정한다.
 * 그래서 Mock → 실서버 전환 때 화면 코드는 바꾸지 않아도 된다.
 *
 * 예정 파일
 *  - AuthRepository / FakeAuthRepository / DefaultAuthRepository
 *  - di/DataModule.kt  (@Binds 로 어떤 구현을 쓸지 결정)
 */
