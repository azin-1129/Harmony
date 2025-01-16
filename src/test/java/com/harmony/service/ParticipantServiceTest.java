package com.harmony.service;

import static org.junit.jupiter.api.Assertions.*;

import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.entity.ChatRoom;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.ChatRoomRepository;
import com.harmony.repository.ParticipantRepository;
import com.harmony.repository.UserRepository;
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
        .profileImageName("orange_orange_cat.png")
        .nickname("치즈고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(cheese);
  }

  // TODO: 채팅방 생성을 Participant로 이관했다면.. 입력 양식 체크도 여기서 해야 되는거 아님?

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
        .chatRoomCountMax(10)
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

  // TODO: 채팅방 삭제 작업은 벌크
  // 채팅방 퇴장
  @DisplayName("채팅방 퇴장 테스트")
  @Order(5)
  @Test
  public void leaveChatRoom(){
    // given
    // 6번 테스트를 위해 단체 채팅방 카운트를 0으로 만든다.
    Long chatRoomId=2L;
    Long userId=1L; // "choco". given
    Integer expectedChatRoomCount=0;

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

  // 예외 테스트

  // 채팅방 참여하려는데 사라지면?
  @DisplayName("채팅방 참여 실패-이미 비활성화된(count=0) 채팅방")
  @Order(6)
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
}