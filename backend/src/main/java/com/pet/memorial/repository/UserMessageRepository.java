package com.pet.memorial.repository;

import com.pet.memorial.entity.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    @Query("""
        select m from UserMessage m
        where (m.senderUsername = :userA and m.receiverUsername = :userB)
           or (m.senderUsername = :userB and m.receiverUsername = :userA)
        order by m.createdAt asc, m.id asc
        """)
    List<UserMessage> findConversation(@Param("userA") String userA, @Param("userB") String userB);

    List<UserMessage> findBySenderUsernameOrReceiverUsernameOrderByCreatedAtDescIdDesc(String senderUsername, String receiverUsername);

    long countByReceiverUsernameAndReadByReceiverFalse(String receiverUsername);
}
