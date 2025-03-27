package com.hunzz.common.exception.custom

import com.hunzz.common.exception.ErrorCode

class InternalSystemException(errorCode: ErrorCode) : RuntimeException(errorCode.message)