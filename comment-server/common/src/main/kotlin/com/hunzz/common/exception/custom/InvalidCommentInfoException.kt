package com.hunzz.common.exception.custom

import com.hunzz.common.exception.ErrorCode

class InvalidCommentInfoException(errorCode: ErrorCode) : RuntimeException(errorCode.message)