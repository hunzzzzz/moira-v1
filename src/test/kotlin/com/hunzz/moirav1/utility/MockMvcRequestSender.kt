package com.hunzz.moirav1.utility

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import java.time.LocalDateTime
import java.util.*

@AutoConfigureMockMvc
abstract class MockMvcRequestSender {
    @Autowired
    lateinit var mockMvc: MockMvc

    fun signup(data: String): MvcResult {
        return mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data)
        ).andDo(print()).andReturn()
    }

    fun login(data: String): MvcResult {
        return mockMvc.perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data)
        ).andDo(print()).andReturn()
    }

    fun logout(atk: String): MvcResult {
        return mockMvc.perform(
            get("/logout")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }

    fun refresh(rtk: String): MvcResult {
        return mockMvc.perform(
            get("/refresh")
                .header("Authorization", rtk)
        ).andDo(print()).andReturn()
    }

    fun getUser(targetId: UUID, atk: String): MvcResult {
        return mockMvc.perform(
            get("/users/${targetId}")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }

    fun follow(targetId: UUID, atk: String): MvcResult {
        return mockMvc.perform(
            get("/users/target/${targetId}/follow")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }

    fun unfollow(targetId: UUID, atk: String): MvcResult {
        return mockMvc.perform(
            get("/users/target/${targetId}/unfollow")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }

    fun getFollowings(userId: UUID, atk: String, cursor: LocalDateTime? = null): MvcResult {
        return mockMvc.perform(
            get("/users/${userId}/followings${if (cursor != null) "?cursor=$cursor" else ""}")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }

    fun getFollowers(userId: UUID, atk: String, cursor: LocalDateTime? = null): MvcResult {
        return mockMvc.perform(
            get("/users/${userId}/followers${if (cursor != null) "?cursor=$cursor" else ""}")
                .header("Authorization", atk)
        ).andDo(print()).andReturn()
    }
}