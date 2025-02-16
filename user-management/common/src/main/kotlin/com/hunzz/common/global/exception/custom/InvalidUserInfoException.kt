package com.hunzz.common.global.exception.custom

import com.hunzz.common.global.exception.ErrorCode

class InvalidUserInfoException(errorCode: ErrorCode) : RuntimeException(errorCode.message)