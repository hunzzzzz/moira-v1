package com.hunzz.api.utility.exception

class InternalSystemException(errorCode: ErrorCode) : RuntimeException(errorCode.message)