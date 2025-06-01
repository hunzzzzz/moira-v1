package com.hunzz.common.repository

import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.model.cache.UserInfo
import com.hunzz.common.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    @Query("SELECT new com.hunzz.common.model.cache.UserAuth(u.id, u.type, u.role, u.email, u.password) FROM User u WHERE u.email = :email")
    fun findUserAuth(email: String): UserAuth?

    @Query("SELECT new com.hunzz.common.model.cache.UserInfo(u.id, u.status, u.name, u.imageUrl, u.thumbnailUrl) FROM User u WHERE u.id = :userId")
    fun findUserProfile(userId: UUID): UserInfo?
}