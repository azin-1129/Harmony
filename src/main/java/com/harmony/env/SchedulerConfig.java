package com.harmony.env;

import com.harmony.entity.*;
import com.harmony.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;
    private final ArticleRepository articleRepository;
    private final BlockRepository blockRepository;

    // 하루마다 새벽에.
    @Scheduled(cron="00 25 17 * * *")
    public void run(){
        // withdrawn_at이 30일 지난 user들을 찾는다.
//        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(31);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(0);
        List<User> withdrawnUsers = userRepository.findByWithdrawnAtBefore(cutoffDate);
        List<Long> withdrawnUserIds=new ArrayList<>();
        for(User user: withdrawnUsers){
            Long userId=user.getUserId();
            // 해당 user가 참여하고 있는 chatroom 정보를 찾는다.
            List<ChatRoom> chatRooms=participantRepository.findChatRoomsByUserId(userId);
            // 참여 정보 등은 아래 채팅 데이터 삭제 스케줄에서 처리

            List<Long> chatroomIds=new ArrayList<>();
            // 해당 채팅방 참여자 count 모두 -1
            for(ChatRoom chatRoom: chatRooms){
                chatroomIds.add(chatRoom.getChatRoomId());
            }
            chatRoomRepository.minusChatRoomCountBulk(chatroomIds);
            log.info("해당 유저가 참여한 채팅방의 참여자 수를 마이너스했습니다.");
            // 관련된 friendships, friendship_requests, blocks, comments, articles 제거
            // friendships
            List<Friendship> friendships=friendshipRepository.findFriendshipsByUserId(userId);
            List<Long> friendshipIds=new ArrayList<>();
            for(Friendship friendship: friendships){
                friendshipIds.add(friendship.getFriendshipId());
            }
            friendshipRepository.deleteFriendshipBulk(friendshipIds);
            log.info("해당 유저의 친구 관계 정보를 모두 삭제했습니다.");

            // friendship_requests
            List<FriendshipRequest> friendshipRequests=friendshipRequestRepository.findFriendshipRequestsByUserId(userId);
            List<Long> friendshipRequestIds=new ArrayList<>();
            for(FriendshipRequest friendshipRequest: friendshipRequests){
                friendshipRequestIds.add(friendshipRequest.getFriendshipRequestId());
            }
            friendshipRequestRepository.deleteFriendshipRequestBulk(friendshipRequestIds);
            log.info("해당 유저와 관련된 친구 신청 이력을 모두 삭제했습니다.");

            // blocks
            // 차단 당하거나, 차단한 유저가 본인일 경우를 쿼리
            // 음.. 복합키 구조를 바꾸는 걸 고려할까?
            List<Block> blocks=blockRepository.findBlocksByUserId(userId);
            List<BlockId> blockIds=new ArrayList<>();
            for(Block block: blocks){
                blockIds.add(block.getBlockId());
            }
            blockRepository.deleteBlockBulk(blockIds);
            log.info("해당 유저와 관련된 차단 이력을 모두 삭제했습니다.");

            // comments
//            List<Comment> comments=commentRepository.findCommentsByUserId(userId);
//            List<Long> commentIds=new ArrayList<>();
//            for(Comment comment: comments){
//                commentIds.add(comment.getCommentId());
//            }
//            commentRepository.deleteCommentBulk(commentIds);

            // articles
            List<Article> articles=articleRepository.findArticlesByAuthorId(userId);
            List<Long> articleIds=new ArrayList<>();
            for(Article article: articles){
                articleIds.add(article.getArticleId());
            }
            articleRepository.deleteArticleBulk(articleIds);
            log.info("해당 유저가 작성한 게시물을 모두 삭제했습니다.");

            withdrawnUserIds.add(userId);
        }

        // user의 email, identifier, nickname 제거
        if(!withdrawnUserIds.isEmpty()){
            userRepository.deleteWithdrawnUserInfoBulk(withdrawnUserIds);
            log.info("해당 유저의 email, identifier, nickname을 모두 제거했습니다.");
        }
    }

    // 한 달마다 새벽에
    // 채팅방 참여자 0인 데이터 정리
}
