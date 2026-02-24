package com.harmony.service;

import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomForceRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.SelectChatRoomParticipantsRequestDto;
import com.harmony.dto.response.SelectChatRoomParticipantsResponseDto;
import com.harmony.dto.response.SelectParticipatedChatRoomsResponseDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.ChatRoomType;
import com.harmony.entity.Participant;
import com.harmony.entity.ParticipantId;
import com.harmony.entity.User;
import com.harmony.exception.GroupChatRoomFullException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ParticipantRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ParticipantService {
  private final ParticipantRepository participantRepository;
  private final ChatRoomService chatRoomService;
  private final UserService userService;
  private final AwsS3Service awsS3Service;

  // 생성+참여
  // 없던 개인 채팅방 생성+참여
  public Long createAndParticipatePersonalChatRoom(Long userId, CreateAndParticipatePersonalChatRoomRequestDto participatePersonalChatRoomRequestDto) {
    User user = userService.getUserById(
        userId);

    User partner = userService.getUserByUserIdentifier(
        participatePersonalChatRoomRequestDto.getPartnerIdentifier());

    // 탈퇴한 사용자와 DM 시도할 경우 생성 불가
    if(partner.getWithdraw()){
      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    // TODO: isPresent() 갈아 끼우기
    Optional<Long> existPersonalChatRoomId = getAlreadyCreatedPersonalChatRoomId(userId, partner.getUserId());

    // TODO: 개인 채팅방이 이미 있는 경우도 예외인가?
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
        .isExited(false)
        .build();

    Participant participantPartner=Participant.builder()
        .participantId(participantPartnerId)
        .user(partner)
        .chatRoom(createdPersonalChatRoom)
        .isExited(false)
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
        .isExited(false)
        .build();

    participantRepository.save(participantUser);

    return createdGroupChatRoomId;
  }

  // 참여

  // 개인(그럴 일 없을 듯)

  // 단체
  public Long participateGroupChatRoom(Long userId, ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto) {
    Long groupChatRoomId=participateGroupChatRoomRequestDto.getChatroomId();

    ParticipantId participantUserId = ParticipantId.builder()
        .userId(userId)
        .chatroomId(groupChatRoomId)
        .build();
    // 재참가라면 isExited=false 설정 후, 그룹 채팅방 id 반환
    if(checkAlreadyParticipatedGroupChatRoom(userId, groupChatRoomId)){
      Participant participant=participantRepository.findByParticipantId(participantUserId);
      participant.updateIsExited(false);
      return groupChatRoomId;
    }

    Optional<ChatRoom> optionalGroupChatRoom=chatRoomService.getChatRoomById(groupChatRoomId);
    // 참여하려는 채팅방이 사라짐
    if(!optionalGroupChatRoom.isPresent()){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }
    ChatRoom groupChatRoom=optionalGroupChatRoom.get();

    // 참가자가 없어 비활성화된 방
    if(groupChatRoom.getChatRoomCount()==0){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }

    // 그룹 채팅방 인원이 꽉 차서 입장할 수 없음
    if(groupChatRoom.getChatRoomCount().equals(groupChatRoom.getChatRoomCountMax())){
      throw new GroupChatRoomFullException(
          ErrorCode.CHATROOM_FULL
      );
    }

    // 인원 수 증가
    groupChatRoom.updateChatRoomCountPositive();

    // 그룹 채팅 참가 처리, 채팅방 id 반환
    User user=userService.getUserById(userId);

    Participant participant=Participant.builder()
        .participantId(participantUserId)
        .user(user)
        .chatRoom(groupChatRoom)
        .isExited(false)
        .build();

    participantRepository.save(participant);

    return groupChatRoomId;
  }

  // 조회

  // 내가 참여한 채팅방 목록 조회
  public List<SelectParticipatedChatRoomsResponseDto> selectParticipatedChatRooms(Long userId){
    List<ChatRoom> chatRooms=participantRepository.findChatRoomsByUserId(userId);

    List<SelectParticipatedChatRoomsResponseDto> filteredChatRooms=new ArrayList<>();

    for(ChatRoom chatRoom:chatRooms){
      Long chatRoomId=chatRoom.getChatRoomId();
      SelectParticipatedChatRoomsResponseDto chatRoomInfo=SelectParticipatedChatRoomsResponseDto.builder()
          .chatRoomId(chatRoomId)
          .chatRoomName(chatRoom.getChatRoomName())
          .chatRoomCount(chatRoom.getChatRoomCount())
          .chatRoomCountMax(chatRoom.getChatRoomCountMax())
          .chatRoomType(chatRoom.getChatRoomType())
          .lastChatMessageId(chatRoom.getLastChatMessageId())
          .chatRoomParticipants(selectSomeChatRoomParticipants(chatRoomId))
          .build();

      filteredChatRooms.add(chatRoomInfo);
    }

    return filteredChatRooms;
  }

  // 특정 채팅방의 일부 참가자 목록 조회(채팅방 썸네일)
  public List<SelectChatRoomParticipantsResponseDto> selectSomeChatRoomParticipants(
      Long chatRoomId){
    List<User> participants=participantRepository.findSomeUsersByChatroomId(chatRoomId, PageRequest.of(0, 5));

    // 중요한 정보를 제외하고 반환
    List<SelectChatRoomParticipantsResponseDto> filteredParticipants=new ArrayList<>();

    for(User participant:participants){
      SelectChatRoomParticipantsResponseDto userInfo= SelectChatRoomParticipantsResponseDto.builder()
          .userId(participant.getUserId())
          .userIdentifier(participant.getUserIdentifier())
          .profileImageUrl(awsS3Service.getUserProfileImageUrl(participant.getProfileImageName()))
          .nickname(participant.getNickname())
          .build();

      filteredParticipants.add(userInfo);
    }

    return filteredParticipants;
  }

  // 특정 채팅방의 참가자 목록 조회
  public List<SelectChatRoomParticipantsResponseDto> selectChatRoomParticipants(
      SelectChatRoomParticipantsRequestDto selectChatRoomParticipantsRequestDto){
    List<User> participants=participantRepository.findUsersByChatroomId(
        selectChatRoomParticipantsRequestDto.getChatRoomId());

    // 중요한 정보를 제외하고 반환
    List<SelectChatRoomParticipantsResponseDto> filteredParticipants=new ArrayList<>();

    for(User participant:participants){
      SelectChatRoomParticipantsResponseDto userInfo= SelectChatRoomParticipantsResponseDto.builder()
          .userId(participant.getUserId())
          .userIdentifier(participant.getUserIdentifier())
          .profileImageUrl(awsS3Service.getUserProfileImageUrl(participant.getProfileImageName()))
          .nickname(participant.getNickname())
          .build();

      filteredParticipants.add(userInfo);
    }

    return filteredParticipants;
  }

  // TODO: is_exited 기반 수정
  // 스스로 퇴장
  public void leaveChatRoom(Long userId, LeaveChatRoomRequestDto leaveChatRoomRequestDto){
    Long chatRoomId=leaveChatRoomRequestDto.getChatRoomId();

    // 이미 강퇴당한 경우
    if(!checkAlreadyParticipatedGroupChatRoom(userId,chatRoomId)){
      throw new EntityNotFoundException(
          ErrorCode.PARTICIPANT_ALREADY_LEFT
      );
    }

    ChatRoom chatRoom=chatRoomService.getChatRoomById(chatRoomId).get();

    // 참가자가 없어 비활성화된 방
    if(chatRoom.getChatRoomCount()==0){
      throw new EntityNotFoundException(
          ErrorCode.CHATROOM_NOT_FOUND
      );
    }

    // 인원 수 감소
    chatRoom.updateChatRoomCountNegative();

    ParticipantId participantId=ParticipantId.builder()
        .userId(userId)
        .chatroomId(chatRoomId)
        .build();

    Participant participant=participantRepository.findByParticipantId(participantId);
    participant.updateIsExited(true);
  }

  // 강제 퇴장
  public void leaveChatRoomForce(LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto){
    Long chatRoomId=leaveChatRoomForceRequestDto.getChatRoomId();
    User leaveForcedUser=userService.getUserByUserIdentifier(leaveChatRoomForceRequestDto.getUserIdentifier());
    Long userId= leaveForcedUser.getUserId();

    // 이미 퇴장했다면 강퇴 불가능
    if(!checkAlreadyParticipatedGroupChatRoom(userId,chatRoomId)){
      throw new EntityNotFoundException(
          ErrorCode.PARTICIPANT_ALREADY_LEFT
      );
    }

    // TODO: isPresent() 갈아 끼우기
    ChatRoom chatRoom=chatRoomService.getChatRoomById(chatRoomId).get();

    // 인원 수 감소
    chatRoom.updateChatRoomCountNegative();

    ParticipantId participantId=ParticipantId.builder()
        .userId(userId)
        .chatroomId(chatRoomId)
        .build();

    Participant participant=participantRepository.findByParticipantId(participantId);
    participant.updateIsExited(true);
  }

  public Optional<Long> getAlreadyCreatedPersonalChatRoomId(Long userId, Long partnerId){
    return participantRepository.findPersonalChatRoomIdByParticipant(ChatRoomType.PERSONAL_CHATROOM, userId, partnerId);
  }
  public boolean checkAlreadyParticipatedGroupChatRoom(Long userId, Long groupChatRoomId) {
    return participantRepository.existsByUserIdAndChatroomId(userId, groupChatRoomId);
  }
}
