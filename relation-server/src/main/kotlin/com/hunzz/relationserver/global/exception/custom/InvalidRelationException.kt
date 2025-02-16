package com.hunzz.relationserver.global.exception.custom

import com.hunzz.relationserver.global.exception.ErrorCode

class InvalidRelationException(errorCode: ErrorCode) : RuntimeException(errorCode.message)