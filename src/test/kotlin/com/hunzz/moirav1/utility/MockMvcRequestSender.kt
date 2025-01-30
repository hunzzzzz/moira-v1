package com.hunzz.moirav1.utility

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

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
}