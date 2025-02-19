package com.hunzz.postserver.global.utility

import com.hunzz.postserver.domain.comment.dto.request.CommentRequest
import com.hunzz.postserver.domain.comment.service.CommentHandler
import com.hunzz.postserver.domain.post.dto.request.PostRequest
import com.hunzz.postserver.domain.post.service.PostHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class DataInitializer(
    private val commentHandler: CommentHandler,
    private val env: Environment,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val postHandler: PostHandler
) : CommandLineRunner {
    private fun addPost(myId: UUID): Long {
        val postRequest = PostRequest(content = "테스트 게시글입니다.", scope = "PUBLIC")

        return postHandler.save(userId = myId, request = postRequest)
    }

    private fun add1000Comments(postId: Long) {
        val idsKey = redisKeyProvider.ids()
        val userIds = redisTemplate.opsForSet().members(idsKey)!!.map { UUID.fromString(it) }
        val commentRequest = CommentRequest(content = "테스트 댓글입니다.")

        repeat(1000) {
            commentHandler.add(userId = userIds.random(), postId = postId, request = commentRequest)
        }
    }

    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            val myId = UUID.fromString("b65996f0-baa1-4e6a-a707-d57236539e93")

            // add post
            val postId = addPost(myId = myId)

            // add 1000 comments
            add1000Comments(postId = postId)
        }
    }
}