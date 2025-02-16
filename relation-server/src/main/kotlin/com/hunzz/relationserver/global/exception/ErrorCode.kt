package com.hunzz.relationserver.global.exception

enum class ErrorCode(val message: String) {
    // relation
    CANNOT_FOLLOW_ITSELF("자신을 팔로우할 수 없습니다."),
    CANNOT_UNFOLLOW_ITSELF("자신을 언팔로우할 수 없습니다."),
    ALREADY_FOLLOWED("이미 팔로우한 대상입니다."),
    ALREADY_UNFOLLOWED("잘못된 요청입니다."),
    FOLLOWING_NOT_EXISTING_USER("존재하지 않는 유저입니다.")
}