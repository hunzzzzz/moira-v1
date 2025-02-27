package com.hunzz.postserver.domain.post.service

import com.hunzz.postserver.domain.post.dto.request.KafkaLikeRequest
import com.hunzz.postserver.domain.post.repository.PostRepository
import com.hunzz.postserver.global.utility.KafkaProducer
import com.hunzz.postserver.global.utility.RedisScriptProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PostScheduler(
    private val kafkaProducer: KafkaProducer,
    private val postRepository: PostRepository,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Scheduled(fixedRate = 1000 * 60) // 1분마다 실행
    fun processLikeNotification() {
        // post:*:like-notification 형태의 키를 조회
        val keys = redisTemplate.keys("post:*:like-notification")

        keys.forEach { key ->
            runBlocking {
                // key에서 postId를 추출한다.
                val postId = key.split(':')[1].toLong()

                val (userIds, postAuthorId) = withTimeout(5_000) {
                    // 작업 1: 스크립트 실행 후 userId 리스트 가져오기
                    val job1 = async {
                        val script = redisScriptProvider.likeQueue()

                        redisTemplate.execute(
                            RedisScript.of(script, List::class.java),
                            listOf(key)
                        ).map { it.toString() }
                    }
                    // 작업 2: postId로 postAuthorId 가져오기
                    val job2 = async {
                        postRepository.getUserIdFromPostId(postId = postId)
                    }
                    awaitAll(job1, job2)
                }

                val kafkaLikeRequest = KafkaLikeRequest(
                    postAuthorId = (postAuthorId as UUID).toString(),
                    postId = postId,
                    userIds = userIds as List<String>
                )

                kafkaProducer.send("like-notification", kafkaLikeRequest)
            }
        }
    }
}