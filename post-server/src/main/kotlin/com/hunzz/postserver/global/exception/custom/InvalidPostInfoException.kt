package com.hunzz.postserver.global.exception.custom

import com.hunzz.postserver.global.exception.ErrorCode

class InvalidPostInfoException(errorCode: ErrorCode) : RuntimeException(errorCode.message)