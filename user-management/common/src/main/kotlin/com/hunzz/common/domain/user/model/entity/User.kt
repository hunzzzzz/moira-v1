package com.hunzz.common.domain.user.model.entity

import com.hunzz.common.domain.user.model.property.UserRole
import com.hunzz.common.domain.user.model.property.UserType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "users")
class User(
    override val id: UUID = UUID.randomUUID(),

    override val email: String,

    override val role: UserRole = UserRole.USER,

    @Column(name = "password", nullable = false)
    val password: String,

    override var name: String
) : BaseUser(
    id = id,
    type = UserType.NORMAL,
    email = email,
    name = name
)