package com.hunzz.common.domain.user.model.entity

import com.hunzz.common.domain.user.model.property.UserType
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "kakao_users")
class KakaoUser(
    override val id: UUID = UUID.randomUUID(),

    override val email: String,

    override var name: String
) : BaseUser(
    id = id,
    type = UserType.KAKAO,
    email = email,
    name = name
)