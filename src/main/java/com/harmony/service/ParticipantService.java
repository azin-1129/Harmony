package com.harmony.service;

import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.ChatRoomType;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ParticipantRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ParticipantService {
  private final ParticipantRepository participantRepository;
  private final ChatRoomService chatRoomService;
  private final UserService userService;

  // 생성+참여
  // 없던 개인 채팅방 생성+참여
  public Long createAndParticipatePersonalChatRoom(Long userId, CreateAndParticipatePersonalChatRoomRequestDto participatePersonalChatRoomRequestDto) {
    User user = userService.getUserById(
        userId);

    User partner = userService.getUserByUserIdentifier(
        participatePersonalChatRoomRequestDto.getPartnerIdentifier());

    Optional<Long> existPersonalChatRoomId = getAlreadyCreatedPersonalChatRoomId(userId, partner.getUserId());

    if (existPersonalChatRoomId.isPresent()) { // 이미 개인 채팅방이 존재
      return existPersonalChatRoomId.get();
    }

    // 개인 채팅방 생성
    ChatRoom createdPersonalChatRoom=chatRoomService.createPersonalChatRoom();

    Long createdPersonalChatRoomId=createdPersonalChatRoom.getChatRoomId();

    // 참가 처리
    ParticipantId participantUserId = ParticipantId.builder()
        .userId(userId)
        .chatroomId(createdPersonalChatRoomId)
        .build();

    ParticipantId participantPartnerId = ParticipantId.builder()
        .userId(partner.getUserId())
        .chatroomId(createdPersonalChatRoomId)
        .build();

    Participant participantUser=Participant.builder()
        .participantId(participantUserId)
        .user(user)
        .chatRoom(createdPersonalChatRoom)
        .build();

    Participant participantPartner=Participant.builder()
        .participantId(participantPartnerId)
        .user(partner)
        .chatRoom(createdPersonalChatRoom)
        .build();

    participantRepository.save(participantUser);
    participantRepository.save(participantPartner);

    return createdPersonalChatRoomId;
  }

  // 없던 그룹 채팅방 생성+참여
  public Long createAndParticipateGroupChatRoom(Long userId, CreateAndParticipateGroupChatRoomRequestDto groupChatRoomCreateRequestDto) {
    User user = userService.getUserById(
        userId);

    ChatRoom createdGroupChatRoom=chatRoomService.createGroupChatRoom(groupChatRoomCreateRequestDto);

    Long createdGroupChatRoomId=createdGroupChatRoom.getChatRoomId();

    ParticipantId participantUserId = ParticipantId.builder()
        .userId(userId)
        .chatroomId(createdGroupChatRoomId)
        .build();

    Participant participantUser=Participant.builder()
        .participantId(participantUserId)
        .user(user)
        .chatRoom(createdGroupChatRoom)
        .build();

    participantRepository.save(participantUser);

    return createdGroupChatRoomId;
  }

  // 참여

  // 개인(그럴 일 없을 듯)

  // 단체
  public Long participateGroupChatRoom(Long userId, ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto) {
    Long groupChatRoomId=participateGroupChatRoomRequestDto.getChatroomId();
    // 이미 그룹 채팅에 참가했다면 그냥 그룹 채팅방 id 반환
    if(checkAlreadyParticipatedGroupChatRoom(userId, groupChatRoomId)){
      return groupChatRoomId;
    }
    ChatRoom groupChatRoom=chatRoomService.getChatRoomById(groupChatRoomId);

    // 참가자가 없어 비활성화된 방
    if(groupChatRoom.getChatRoomCount()==0){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }

    // 인원 수 증가
    groupChatRoom.updateChatRoomCountPositive();

    // 그룹 채팅 참가 처리, 채팅방 id 반환
    ParticipantId participantUserId = ParticipantId.builder()
        .userId(userId)
        .chatroomId(groupChatRoomId)
        .build();

    Participant participant=Participant.builder()
        .participantId(participantUserId)
        .build();

    participantRepository.save(participant);

    return groupChatRoomId;
  }

  // 퇴장
  public void leaveChatRoom(Long userId, LeaveChatRoomRequestDto leaveChatRoomRequestDto){
    Long chatRoomId=leaveChatRoomRequestDto.getChatRoomId();

    ParticipantId participantId=ParticipantId.builder()
        .userId(userId)
        .chatroomId(chatRoomId)
        .build();

    ChatRoom chatRoom=chatRoomService.getChatRoomById(chatRoomId);

    // 참가자가 없어 비활성화된 방
    if(chatRoom.getChatRoomCount()==0){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }

    // 인원 수 감소
    chatRoom.updateChatRoomCountNegative();
    participantRepository.deleteById(participantId);
  }

  public Optional<Long> getAlreadyCreatedPersonalChatRoomId(Long userId, Long partnerId){
    return participantRepository.findPersonalChatRoomIdByParticipant(ChatRoomType.PERSONAL_CHATROOM, userId, partnerId);
  }
  public boolean checkAlreadyParticipatedGroupChatRoom(Long userId, Long groupChatRoomId) {
    return participantRepository.existsByUserIdAndChatroomId(userId, groupChatRoomId);
  }
}
