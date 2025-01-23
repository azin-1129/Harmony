package com.harmony.service;

import static org.junit.jupiter.api.Assertions.*;

import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomForceRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.dto.request.SelectChatRoomParticipantsRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.exception.GroupChatRoomFullException;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ChatRoomRepository;
import com.harmony.repository.ParticipantRepository;
import com.harmony.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 참여한 채팅방 목록에서 진입하는 것이 아닌,
 채팅방 글이나 팝업에서 채팅방 참가하기 버튼 눌렀을 때를 상정함.

 1:1 대화
 생성된 1:1 방이 없다면, 채팅방 생성 후 chatRoomId 반환
 생성된 1:1 방이 있다면, 채팅방 생성하지 않고 chatRoomId 반환

 단체 대화
 참가하지 않은 방이라면, 참가 처리 후 채팅방 생성, chatRoomId 반환
 참가한 방이라면, 참가 처리 하지 않고 chatRoomId 반환
 **/
@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParticipantServiceTest {
  @Autowired
  private ParticipantService participantService;

  @Autowired
  private ParticipantRepository participantRepository;

  @Autowired
  private ChatRoomService chatRoomService;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  // test에 쓰일 user 정보 등록
  @BeforeAll
  void setUp(){
    User choco= User.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .password("azin1129!")
        .profileImageName("orange_orange_cat.png")
        .nickname("초코고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(choco);

    User cheese= User.builder()
        .email("azin@google.com")
        .userIdentifier("cheese")
        .password("azin1129!")
        .profileImageName("cheese_cat.png")
        .nickname("치즈고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(cheese);

    User oreo= User.builder()
        .email("azin@kakao.com")
        .userIdentifier("oreo")
        .password("azin1129!")
        .profileImageName("oreo_cat.png")
        .nickname("오레오 고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(oreo);

    User chaos= User.builder()
        .email("azin@samsung.com")
        .userIdentifier("chaos")
        .password("azin1129!")
        .profileImageName("chaos_gatto.png")
        .nickname("알록달록 고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(chaos);
  }

  // 개인 채팅방
  // 첫 생성
  @DisplayName("채팅방 생성+참여: 개인 테스트")
  @Order(1)
  @Test
  public void createAndParticipateUncreatedPersonalChatRoom(){
    // given
    Long expectedChatRoomId=1L;
    Long userId=1L; // "choco". given
    String partnerIdentifier="cheese"; // "cheese"

    CreateAndParticipatePersonalChatRoomRequestDto participatePersonalChatRoomRequestDto= CreateAndParticipatePersonalChatRoomRequestDto.builder()
        .partnerIdentifier(partnerIdentifier)
        .build();

    // 생성된 1:1 방이 없다면, 채팅방 생성 후 참가 처리, chatRoomId 반환
    // when
    Long createdChatRoomId=participantService.createAndParticipatePersonalChatRoom(userId, participatePersonalChatRoomRequestDto);

    // then
    // 이미 개인 채팅방이 있다면 count는 2(비활성화가 필요할듯)
    assertEquals(expectedChatRoomId, createdChatRoomId);
  }

  // 이미 생성됨
  @DisplayName("채팅방 참여: 개인(이미 생성됨)")
  @Order(2)
  @Test
  public void participateCreatedPersonalChatRoom(){
    // given
    Long expectedChatRoomId=1L;
    Long userId=1L; // "choco". given
    String partnerIdentifier="cheese"; // "cheese"

    CreateAndParticipatePersonalChatRoomRequestDto participatePersonalChatRoomRequestDto= CreateAndParticipatePersonalChatRoomRequestDto.builder()
        .partnerIdentifier(partnerIdentifier)
        .build();

    // when
    Long createdChatRoomId=participantService.createAndParticipatePersonalChatRoom(userId, participatePersonalChatRoomRequestDto);

    // 참가 처리

    // then
    // 이미 개인 채팅방이 있다면 count는 2(비활성화가 필요할듯)
    assertEquals(expectedChatRoomId, createdChatRoomId);
  }

  // 단체 채팅방(누군가 직접 생성해야만 참여할 수 있음.)
  // 첫 생성
  @DisplayName("채팅방 생성+참여: 단체 테스트")
  @Order(3)
  @Test
  public void createAndParticipateUncreatedGroupChatRoom(){
    // given
    Long expectedChatRoomId= 2L; // 앞서 생성된 개인 채팅방이 있기 때문에 2
    Long userId=1L; // "choco". given

    // 2번 오픈 채팅방 생성
    CreateAndParticipateGroupChatRoomRequestDto createAndParticipateGroupChatRoomRequestDto= CreateAndParticipateGroupChatRoomRequestDto.builder()
        .chatRoomName("심심한 사람만")
        .chatRoomCountMax(3)
        .build();

    Long createdChatRoomId=participantService.createAndParticipateGroupChatRoom(userId, createAndParticipateGroupChatRoomRequestDto);

    // 그룹 채팅방 생성 후, 참가 여부 검증
    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(createdChatRoomId)
        .build();

    // when

    // then
    // 0명이면 삭제 처리 or 비활성화
    assertEquals(createdChatRoomId, participantService.participateGroupChatRoom(userId, participateGroupChatRoomRequestDto));
  }

  // 이미 생성됨
  @DisplayName("단체 채팅방 참여 테스트-이미 참가함")
  @Order(4)
  @Test
  public void participateCreatedGroupChatRoom(){
    // given
    Long createdChatRoomId=2L; // 3번 테스트에서 생성한 2번 오픈 채팅방
    Long userId=1L; // "choco". given

    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(createdChatRoomId)
        .build();

    // when

    // then
    // 3번 테스트에서 채팅방을 생성했으니 반환 값은 2여야 한다.
    // TODO: 이러면 이미 참가했던 아니던 반환 값이 똑같은데 ..
    assertEquals(createdChatRoomId, participantService.participateGroupChatRoom(userId, participateGroupChatRoomRequestDto));
  }

  // 내가 참여한 채팅방 목록 조회
  @DisplayName("내가 참여한 채팅방 목록 조회 테스트")
  @Order(5)
  @Test
  public void selectParticipatedChatRooms(){
    // given
    Long userId=1L; // 1, 2번 방 참여중

    // when

    // then
    assertNotNull(participantService.selectParticipatedChatRooms(userId));
  }

  // 특정 채팅방의 참가자 목록 조회
  @DisplayName("특정 채팅방 참가자 목록 조회 테스트")
  @Order(6)
  @Test
  public void selectChatRoomParticipants(){
    // given
    Long chatRoomId=1L; // 1,2번 user 참여중인 DM방

    SelectChatRoomParticipantsRequestDto selectChatRoomParticipantsRequestDto=SelectChatRoomParticipantsRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    // when

    // then
    assertNotNull(participantService.selectChatRoomParticipants(selectChatRoomParticipantsRequestDto));
  }

  // TODO: 채팅방 삭제 작업은 벌크
  // 채팅방 퇴장
  @DisplayName("채팅방 퇴장 테스트")
  @Order(7)
  @Test
  public void leaveChatRoom(){
    // given
    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(2L)
        .build();

    // 1번 user가 참여하고 있는 2번 방(그룹)에 2번 user를 참여 시킨다.
    participantService.participateGroupChatRoom(2L, participateGroupChatRoomRequestDto);

    // 9번 테스트를 위해 1번 user를 퇴장 시키고 단체 채팅방 카운트-1
    Long chatRoomId=2L;
    Long userId=1L; // "choco". given
    Integer expectedChatRoomCount=1;

    LeaveChatRoomRequestDto leaveChatRoomRequestDto=LeaveChatRoomRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    // when
    // 해당 채팅방 카운트-=1
    participantService.leaveChatRoom(userId, leaveChatRoomRequestDto);
    ChatRoom chatRoom=chatRoomRepository.findById(chatRoomId).get();

    // then
    assertEquals(expectedChatRoomCount, chatRoom.getChatRoomCount());
  }

  // 채팅방 강제 퇴장
  @DisplayName("채팅방 강제 퇴장 테스트")
  @Order(8)
  @Test
  public void leaveChatRoomForce(){
    // 9번 테스트를 위해 2번 user를 퇴장 시키고 단체 채팅방 카운트-1. 이로써 2번 방 Count는 0이 된다.
    // given
    String userIdentifier="cheese";
    Long chatRoomId=2L;
    ChatRoom chatRoom=chatRoomRepository.findById(chatRoomId).get();
    Integer chatRoomCountBeforeLeave=chatRoom.getChatRoomCount();

    LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto= LeaveChatRoomForceRequestDto.builder()
        .chatRoomId(chatRoomId)
        .userIdentifier(userIdentifier)
        .build();

    // when
    participantService.leaveChatRoomForce(leaveChatRoomForceRequestDto);

    chatRoom=chatRoomRepository.findById(chatRoomId).get();
    Integer chatRoomCountAfterLeave=chatRoom.getChatRoomCount();

    // then
    assertEquals(chatRoomCountBeforeLeave-1, chatRoomCountAfterLeave);
  }

  // 예외 테스트

  // 채팅방 참여하려는데 사라지면?
  @DisplayName("채팅방 참여 실패-이미 비활성화된(count=0) 채팅방")
  @Order(9)
  @Test
  public void participateChatRoomFailedByAlreadyDeleted(){
    // 만약 참여하려는 채팅방의 카운트가 0이면 exception을 던진다.
    // given
    Long chatRoomId=2L;
    Long userId=1L;

    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto= ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(chatRoomId)
        .build();

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
    -> participantService.participateGroupChatRoom(userId, participateGroupChatRoomRequestDto));
  }

  // 그룹 채팅방 인원이 꽉 찼다면?
  @DisplayName("채팅방 참여 실패-만원인 단체 채팅방")
  @Order(10)
  @Test
  public void participateGroupChatRoomFailedByFull(){
    // TODO: 새로 3번 그룹 채팅방 만들고 만원 처리
    // 새로운 3번 그룹 채팅방 생성
    // 2번 오픈 채팅방 생성
    Long userId=1L; // "choco". given

    CreateAndParticipateGroupChatRoomRequestDto createAndParticipateGroupChatRoomRequestDto= CreateAndParticipateGroupChatRoomRequestDto.builder()
        .chatRoomName("3번방의 선물")
        .chatRoomCountMax(3)
        .build();

    Long createdChatRoomId=participantService.createAndParticipateGroupChatRoom(userId, createAndParticipateGroupChatRoomRequestDto);

    // 2번, 3번 user 참가로 만원 처리
    ParticipateGroupChatRoomRequestDto user2ParticipateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(createdChatRoomId)
        .build();

    participantService.participateGroupChatRoom(2L, user2ParticipateGroupChatRoomRequestDto);

    ParticipateGroupChatRoomRequestDto user3ParticipateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(createdChatRoomId)
        .build();

    participantService.participateGroupChatRoom(3L, user3ParticipateGroupChatRoomRequestDto);

    log.info("1,2,3번 user 참가 완료");

    // given
    Long newUserId=4L;

    ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto=ParticipateGroupChatRoomRequestDto.builder()
        .chatroomId(createdChatRoomId)
        .build();

    // when

    // then
    assertThrows(GroupChatRoomFullException.class, ()
        -> participantService.participateGroupChatRoom(newUserId, participateGroupChatRoomRequestDto));
  }

  // 탈퇴한 사용자와는 DM을 생성할 수 없음
  @DisplayName("개인 채팅방 참가+생성 실패-이미 탈퇴한 유저 테스트")
  @Order(11)
  @Test
  public void createAndParticipateGroupChatRooFailedByAlreadyWithdraw() throws Exception {
    // given
    // TODO: 탈퇴 처리 필요
    // 3번 user 탈퇴 처리
    userService.deleteUser(3L);

    // 1번, 3번 user DM 시도
    Long userId=1L;
    String partnerIdentifier="oreo";

    CreateAndParticipatePersonalChatRoomRequestDto createAndParticipatePersonalChatRoomRequestDto= CreateAndParticipatePersonalChatRoomRequestDto.builder()
        .partnerIdentifier(partnerIdentifier)
        .build();

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
        -> participantService.createAndParticipatePersonalChatRoom(userId, createAndParticipatePersonalChatRoomRequestDto));
  }

  // TODO: 방장 지정은 어캄?
  // 방장 입장에서 강퇴하려는데 이미 나갔다면 Failed
  @DisplayName("강퇴 실패-이미 퇴장한 유저 테스트")
  @Order(12)
  @Test
  public void leaveChatRoomForceFailedByAlreadyLeft() throws Exception{
    // given
    Long chatRoomId=3L;
    // 3번 방 방장이 1번이라고 가정한다.
    // 3번 user 퇴장 처리
    LeaveChatRoomRequestDto leaveChatRoomRequestDto=LeaveChatRoomRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    participantService.leaveChatRoom(3L, leaveChatRoomRequestDto);

    String leaveForcedUserIdentifier="oreo";

    // 1번 user가 이미 퇴장한 3번 user 강퇴 시도
    LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto= LeaveChatRoomForceRequestDto.builder()
        .chatRoomId(chatRoomId)
        .userIdentifier(leaveForcedUserIdentifier)
        .build();

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
        -> participantService.leaveChatRoomForce(leaveChatRoomForceRequestDto));
  }

  // 참가자 입장에서 퇴장하려는데 이미 강퇴당했다면?
  @DisplayName("퇴장 실패-이미 강퇴당한 유저 테스트")
  @Order(13)
  @Test
  public void leaveChatRoomFailedByAlreadyForced() throws Exception{
    // given
    // 3번 방 방장이 1번이라고 가정한다.
    Long chatRoomId=3L;

    // 2번 user 강제퇴장 처리
    String leaveForcedUserIdentifier="cheese";
    LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto= LeaveChatRoomForceRequestDto.builder()
        .chatRoomId(chatRoomId)
        .userIdentifier(leaveForcedUserIdentifier)
        .build();

    participantService.leaveChatRoomForce(leaveChatRoomForceRequestDto);

    // 2번 user가 3번방 퇴장 시도
    LeaveChatRoomRequestDto leaveChatRoomRequestDto=LeaveChatRoomRequestDto.builder()
        .chatRoomId(chatRoomId)
        .build();

    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
        -> participantService.leaveChatRoom(3L, leaveChatRoomRequestDto));
  }
}