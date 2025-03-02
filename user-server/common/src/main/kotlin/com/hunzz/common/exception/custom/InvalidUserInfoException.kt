package com.hunzz.common.exception.custom

import com.hunzz.common.exception.ErrorCode

class InvalidUserInfoException(errorCode: ErrorCode) : RuntimeException(errorCode.message)