package com.hunzz.common.global.exception.custom

import com.hunzz.common.global.exception.ErrorCode

class InternalSystemException(errorCode: ErrorCode) : RuntimeException(errorCode.message)