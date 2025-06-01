package com.hunzz.authserver.utility.exception.custom

import com.hunzz.authserver.utility.exception.ErrorCode

class InvalidAuthException(errorCode: ErrorCode) : RuntimeException(errorCode.message)