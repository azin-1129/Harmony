package com.harmony.env.util;

import com.harmony.entity.Article;
import com.harmony.entity.Block;
import com.harmony.entity.BlockId;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.Comment;
import com.harmony.entity.Friendship;
import com.harmony.entity.FriendshipRequest;
import com.harmony.entity.Heart;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import com.harmony.entity.User;
import com.harmony.repository.ArticleRepository;
import com.harmony.repository.BlockRepository;
import com.harmony.repository.ChatRoomRepository;
import com.harmony.repository.CommentRepository;
import com.harmony.repository.FriendshipRepository;
import com.harmony.repository.FriendshipRequestRepository;
import com.harmony.repository.HeartRepository;
import com.harmony.repository.ParticipantRepository;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class SchedulerUtil {
  private final UserRepository userRepository;
  private final ParticipantRepository participantRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final FriendshipRepository friendshipRepository;
  private final FriendshipRequestRepository friendshipRequestRepository;
  private final BlockRepository blockRepository;
  private final ArticleRepository articleRepository;
  private final HeartRepository heartRepository;
  private final CommentRepository commentRepository;

  public void deleteWithdrawnUsersDataWithoutChatInfo(){
    // 참여 정보 등은 채팅 데이터 삭제 스케줄에서 처리

    // withdrawn_at이 30일 지난 user들을 찾는다.
//        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(31);
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(0);
    List<User> withdrawnUsers = userRepository.findByWithdrawnAtBefore(cutoffDate);
    List<Long> withdrawnUserIds=new ArrayList<>();
    for(User user: withdrawnUsers){
      Long userId=user.getUserId();

      // heart
      List<Heart> hearts=user.getHearts();
      List<Long> heartIds=new ArrayList<>();
      List<Long> releatedArticleIds=new ArrayList<>();
      for(Heart heart: hearts) {
        log.info(heart.toString());
        heartIds.add(heart.getHeartId());
        Article relatedArticle = heart.getArticle();
        releatedArticleIds.add(relatedArticle.getArticleId());
      }

      // comments
      List<Comment> comments=commentRepository.findCommentsByUserId(userId);
      List<Long> commentIds=new ArrayList<>();
      for(Comment comment: comments){
        Article relatedArticle=comment.getArticle();
        log.debug("댓글 카운트를 감소합니다:"+relatedArticle.getArticleCommentCount());
        relatedArticle.updateArticleCommentCount(false);
        log.debug("댓글 카운트를 감소했습니다:"+relatedArticle.getArticleCommentCount());
        commentIds.add(comment.getCommentId());
      }

      // 업데이트 작업 먼저
      articleRepository.updateArticleCommentCountsBulk(releatedArticleIds);
      articleRepository.updateArticleLikeCountsBulk(releatedArticleIds);
      commentRepository.deleteCommentsBulk(commentIds);
      heartRepository.deleteHeartsBulk(heartIds);

      // chatrooms
      List<ChatRoom> chatRooms=participantRepository.findChatRoomsByUserId(userId);
      List<Long> chatroomIds=new ArrayList<>();
      // 해당 채팅방 참여자 count 모두 -1
      for(ChatRoom chatRoom: chatRooms){
        chatroomIds.add(chatRoom.getChatRoomId());
      }
      chatRoomRepository.minusChatRoomCountBulk(chatroomIds);
      log.info("해당 유저가 참여한 채팅방의 참여자 수를 마이너스했습니다.");

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
      List<Block> blocks=blockRepository.findBlocksByUserId(userId);
      List<BlockId> blockIds=new ArrayList<>();
      for(Block block: blocks){
        blockIds.add(block.getBlockId());
      }
      blockRepository.deleteBlockBulk(blockIds);
      log.info("해당 유저와 관련된 차단 이력을 모두 삭제했습니다.");

      // articles
      List<Article> articles=articleRepository.findArticlesByAuthorId(userId);
      List<Long> articleIds=new ArrayList<>();
      for(Article article: articles){
        articleIds.add(article.getArticleId());
      }
      articleRepository.deleteArticlesBulk(articleIds);
      log.info("해당 유저가 작성한 게시물을 모두 삭제했습니다.");

      withdrawnUserIds.add(userId);
      user.updateWithDraw(false); // 다음 스케줄에 처리하지 않도록 방지
    }

    // user 제거
    if(!withdrawnUserIds.isEmpty()){
            userRepository.deleteWithdrawnUserBulk(withdrawnUserIds);
      log.info("탈퇴 유저 데이터를 제거했습니다.");
    }
  }

  public void deleteArticlesInfo(){
    List<Article> articles=articleRepository.findByIsDeleted(true);
    List<Long> articleIds=new ArrayList<>();
    List<Long> heartIds=new ArrayList<>();
    List<Long> commentIds=new ArrayList<>();
    for(Article article: articles){
      Long articleId=article.getArticleId();
      articleIds.add(articleId);

      List<Heart> hearts=heartRepository.findHeartsByArticleId(articleId);
      for(Heart heart: hearts){
        heartIds.add(heart.getHeartId());
      }

      List<Comment> comments=commentRepository.findCommentsByArticleId(articleId);
      for(Comment comment: comments){
        commentIds.add(comment.getCommentId());
      }

      heartRepository.deleteHeartsBulk(heartIds);
      commentRepository.deleteCommentsBulk(commentIds);
    }
    articleRepository.deleteArticlesBulk(articleIds);
  }

  public void deleteZeroCountChatRoomsInfo(){
    // 참여자 수가 0인 채팅방들
    List<ChatRoom> chatRooms=chatRoomRepository.findAllZeroCountChatRooms();
    List<Long> chatRoomIds=new ArrayList<>();
    for(ChatRoom chatRoom: chatRooms){
      chatRoomIds.add(chatRoom.getChatRoomId());
    }

    // 탈퇴자들 정보
    List<User> withdrawnUsers=userRepository.findByWithdraw(true);
    List<Long> withdrawnUserIds=new ArrayList<>();
    for(User user: withdrawnUsers){
      withdrawnUserIds.add(user.getUserId());
    }

    // 탈퇴자들의 id가 포함되고+참여자 수가 0인 채팅방들의 id가 포함되는 참여자 정보 조회
    List<Participant> softDeletedParticipants=participantRepository.findSoftDeletedParticipants(withdrawnUserIds, chatRoomIds);
    log.info("soft delete된 participant read 완료");
    List<ParticipantId> softDeletedParticipantIds=new ArrayList<>();
    for(Participant participant: softDeletedParticipants){
      softDeletedParticipantIds.add(participant.getParticipantId());
      log.info("participants id 정보 추가중 ...");
    }

    // 참여자 정보 먼저 delete
    participantRepository.deleteSoftDeletedParticipantsBulk(softDeletedParticipantIds);
    log.info("soft delete된 participant 정보를 제거했습니다.");

    chatRoomRepository.deleteChatRoomsBulk(chatRoomIds);
    log.info("참가자 수가 0인 채팅방 데이터를 제거했습니다.");
  }
}
