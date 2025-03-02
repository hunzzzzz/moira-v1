package com.hunzz.common.exception.custom

import com.hunzz.common.exception.ErrorCode

class InvalidSignupException(errorCode: ErrorCode) : RuntimeException(errorCode.message)