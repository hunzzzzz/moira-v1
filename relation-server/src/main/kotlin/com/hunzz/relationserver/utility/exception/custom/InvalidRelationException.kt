package com.hunzz.relationserver.utility.exception.custom

import com.hunzz.relationserver.utility.exception.ErrorCode

class InvalidRelationException(errorCode: ErrorCode) : RuntimeException(errorCode.message)