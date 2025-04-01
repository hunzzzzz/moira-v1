package com.hunzz.data.task

import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.repository.PostRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.time.LocalDate
import java.util.*

@Component
class DeletePostTask(
    private val jdbcTemplate: JdbcTemplate,
    private val kafkaProducer: KafkaProducer,
    private val postRepository: PostRepository
) {
    private fun UUID.toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(16)

        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)

        return byteBuffer.array()
    }

    @Scheduled(cron = "0 0 4 * * ?")
    fun deletePost() {
        // 삭제할 게시글들의 id 조회
        val deletePostsIds = postRepository.findPostIdsByStatusAndDeletedAt(now = LocalDate.now())
        if (deletePostsIds.isEmpty()) return

        // 배치 삭제
        val sql = "DELETE FROM posts WHERE post_id = ?"

        jdbcTemplate.batchUpdate(sql, deletePostsIds, 1000) { ps, postId ->
            ps.setBytes(1, postId.toBytes())
        }

        // 해당 게시글과 관련된 기타 데이터(댓글, 이미지) 역시 삭제한다.
        kafkaProducer.send(topic = "delete-post", data = deletePostsIds)
    }
}