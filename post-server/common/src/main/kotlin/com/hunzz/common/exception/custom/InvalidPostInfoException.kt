package com.hunzz.common.exception.custom

import com.hunzz.common.exception.ErrorCode

class InvalidPostInfoException(errorCode: ErrorCode) : RuntimeException(errorCode.message)