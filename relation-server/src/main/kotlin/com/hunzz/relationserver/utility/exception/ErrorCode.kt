package com.hunzz.relationserver.utility.exception

enum class ErrorCode(val message: String) {
    // relation
    CANNOT_FOLLOW_ITSELF("자신을 팔로우할 수 없습니다."),
    CANNOT_UNFOLLOW_ITSELF("자신을 언팔로우할 수 없습니다."),
    ALREADY_FOLLOWED("이미 팔로우한 대상입니다."),
    ALREADY_UNFOLLOWED("잘못된 요청입니다."),

    // feign
    FEIGN_CLIENT_ERROR("FeignClient 요청에 실패하였습니다.")
}