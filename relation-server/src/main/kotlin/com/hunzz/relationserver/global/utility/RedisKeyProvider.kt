package com.hunzz.relationserver.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // auth
    fun userAuth(email: String) = "auth::$email"
    fun rtk(email: String) = "rtk::$email"
    fun blockedAtk(atk: String) = "blocked_atk::$atk"

    // admin
    fun adminCode() = "admin_signup_code"
    fun bannedUsers() = "banned_users"

    // user
    fun emails() = "emails"
    fun ids() = "ids"

    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"

    // post
    fun like(userId: UUID) = "like:$userId"
    fun likeCount() = "likes"
}