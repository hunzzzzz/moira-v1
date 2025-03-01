package com.hunzz.common.domain.user.repository

import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.model.entity.KakaoUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface KakaoUserRepository : JpaRepository<KakaoUser, UUID> {
    @Query("SELECT new com.hunzz.common.domain.user.model.UserAuth(u.id, u.type, u.role, u.email, null) FROM KakaoUser u WHERE u.email = :email")
    fun findUserAuth(email: String): UserAuth?
}