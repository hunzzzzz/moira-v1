package com.hunzz.relationserver.utility.exception.custom

import com.hunzz.relationserver.utility.exception.ErrorCode

class InternalSystemError(errorCode: ErrorCode) : RuntimeException(errorCode.message)