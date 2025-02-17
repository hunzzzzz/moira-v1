package com.hunzz.common.domain.user.repository

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    @Query("SELECT new com.hunzz.common.domain.user.model.UserAuth(u.id, u.role, u.email, u.password) FROM User u WHERE u.email = :email")
    fun findUserAuth(email: String): UserAuth?

    @Query("SELECT new com.hunzz.common.domain.user.model.CachedUser(u.id, u.status, u.name, u.imageUrl) FROM User u WHERE u.id = :userId")
    fun findUserProfile(userId: UUID): CachedUser?
}