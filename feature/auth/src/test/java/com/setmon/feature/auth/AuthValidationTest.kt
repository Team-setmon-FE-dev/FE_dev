package com.setmon.feature.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun email() {
        assertTrue(AuthValidation.isValidEmail("yeeun@setmon.app"))
        assertTrue(AuthValidation.isValidEmail("  a.b+c@mail.co.kr "))
        assertFalse(AuthValidation.isValidEmail("yeeun"))
        assertFalse(AuthValidation.isValidEmail("yeeun@setmon"))
        assertFalse(AuthValidation.isValidEmail("@setmon.app"))
    }

    @Test
    fun password_atLeast8() {
        assertFalse(AuthValidation.isValidPassword("1234567"))
        assertTrue(AuthValidation.isValidPassword("12345678"))
        assertTrue(AuthValidation.isValidPassword("a".repeat(64)))
        assertFalse(AuthValidation.isValidPassword("a".repeat(65)))
    }

    @Test
    fun nickname_2to10_afterTrim() {
        assertFalse(AuthValidation.isValidNickname("y"))
        assertFalse(AuthValidation.isValidNickname("  y  "))
        assertTrue(AuthValidation.isValidNickname("예은"))
        assertTrue(AuthValidation.isValidNickname("abcdefghij"))
        assertFalse(AuthValidation.isValidNickname("abcdefghijk"))
    }
}
