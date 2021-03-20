package com.nubip.agrobib_messenger.utils

class PasswordSignUpValidator {
    companion object {
        val SUCCESSFUL_VALIDATION = "Validation was successful!"

        //  Пароль должен быть не меньше 8 символов и содержать минимум 1 цифру и символ
        fun validatePassword(password: String): String {
            val atLeastOneDigit = Regex(pattern = """\d+""")
            val atLeastOneSymbol = Regex(pattern = """\w+""")

            if (password.length <= 8) {
                return "Password must be at least 8 characters long!"
            }

            if (!atLeastOneDigit.containsMatchIn(password)) {
                return "Password must contain at least 1 digit!"
            }

            if (!atLeastOneSymbol.containsMatchIn(password)) {
                return "Password must contain at least 1 symbol!"
            }

            return SUCCESSFUL_VALIDATION
        }

        fun validateEmail(email: String): String {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return "Email address is incorrect!"
            }

            return SUCCESSFUL_VALIDATION
        }

        fun validateNickname(nickname: String): String {
            if (nickname.length < 4) {
                return "Nickname is too short"
            }

            if (nickname.length > 16) {
                return "Nickname is too long"
            }

            return SUCCESSFUL_VALIDATION
        }
    }
}