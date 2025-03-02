package com.hunzz.common.model.entity

import com.hunzz.common.model.entity.BaseUser
import com.hunzz.common.model.property.UserType
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "naver_users")
class NaverUser(
    override val id: UUID = UUID.randomUUID(),

    override val email: String,

    override var name: String
) : BaseUser(
    id = id,
    type = UserType.NAVER,
    email = email,
    name = name
)