package com.harmony.service;

import static org.junit.jupiter.api.Assertions.*;

import com.harmony.dto.request.CreateFriendshipRequestDto;
import com.harmony.dto.response.ReceivedFriendshipRequestResponseDto;
import com.harmony.dto.response.SentFriendshipRequestResponseDto;
import com.harmony.entity.FriendshipRequest;
import com.harmony.entity.FriendshipRequestStatus;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.exception.AlreadyCanceledFriendshipRequestException;
import com.harmony.repository.FriendshipRequestRepository;
import com.harmony.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
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

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendshipRequestServiceTest {
  @Autowired
  private FriendshipRequestService friendshipRequestService;

  @Autowired
  private FriendshipRequestRepository friendshipRequestRepository;

  @Autowired
  private UserRepository userRepository;

  // 3명 회원가입
  User choco, cheese, oreo;
  @BeforeAll
  void setUp(){
    choco= User.builder()
        .email("azin@naver.com")
        .userIdentifier("choco")
        .password("azin1129!")
        .profileImageName("orange_orange_cat.png")
        .nickname("초코고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(choco);
    log.info("초기 1번 user의 상태:"+choco.toString());

    cheese= User.builder()
        .email("azin@google.com")
        .userIdentifier("cheese")
        .password("azin1129!")
        .profileImageName("cheese_cat.png")
        .nickname("치즈고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(cheese);

    oreo= User.builder()
        .email("azin@kakao.com")
        .userIdentifier("oreo")
        .password("azin1129!")
        .profileImageName("oreo_cat.png")
        .nickname("오레오 고양이")
        .withdraw(false)
        .role(Role.MEMBER)
        .build();

    userRepository.save(oreo);
  }

  // 친구 추가 요청
  @DisplayName("친구 추가 요청 테스트")
  @Order(1)
  @Test
  public void createFriendshipRequest(){
    // given
    Long fromUserId=1L;
    Long toUserId=2L;
    String fromUserIdentifier="choco";
    String toUserIdentifier="cheese";

    // 양쪽 request list에 저장될 dto
    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    // when request는 a to b, b to a로 저장 되어야 역요청이 성립하지 않음
    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    // then
    User fromUser=userRepository.findById(fromUserId).get();
    User toUser=userRepository.findById(toUserId).get();
    log.info("갱신된 choco 정보:"+fromUser);
    log.info("갱신된 cheese 정보:"+toUser);

    FriendshipRequest friendshipRequest=(FriendshipRequest) fromUser.getSentFriendshipRequests().toArray()[0];
    Long fromUserId2=1L;
    Long toUserId2=2L;

    assertNotNull(friendshipRequest);
    assertEquals(fromUserId2, friendshipRequest.getFriendshipRequestSender().getUserId());
    assertEquals(toUserId2, friendshipRequest.getFriendshipRequestReceiver().getUserId());
    assertEquals(FriendshipRequestStatus.PENDING, friendshipRequest.getFriendshipRequestStatus());
  }

  // 친구 수락
  @DisplayName("친구 수락(A-B)")
  @Order(2)
  @Test
  public void AcceptFriendshipRequest(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    FriendshipRequest friendshipRequest=(FriendshipRequest) userRepository.findById(fromUserId).get().getSentFriendshipRequests().toArray()[0];
    log.info("요청 되긴 했나요?:"+friendshipRequest.toString());
    // when
    // 보낸 요청, 받은 요청 없애고 friend에 insert
    friendshipRequestService.acceptFriendshipRequest(toUserId, fromUserIdentifier);

    // then
    User toUser=userRepository.findById(toUserId).get();
    User fromUser=userRepository.findById(fromUserId).get();

    log.info("갱신된 요청: "+toUser.getReceivedFriendshipRequests());
    log.info("갱신된 요청: "+fromUser.getSentFriendshipRequests());

    log.info(toUser.toString());
    log.info(fromUser.toString());
    FriendshipRequest sentFriendshipRequest=(FriendshipRequest) fromUser.getSentFriendshipRequests().toArray()[0];
    FriendshipRequest receivedFriendshipRequest=(FriendshipRequest) toUser.getReceivedFriendshipRequests().toArray()[0];

    assertEquals(FriendshipRequestStatus.ACCEPTED, sentFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(FriendshipRequestStatus.ACCEPTED, receivedFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(1, toUser.getFriendships().size());
    assertEquals(1, fromUser.getFriendships().size());
  }

  // 친구 거절
  @DisplayName("요청 거절(A-B)")
  @Order(3)
  @Test
  public void RejectFriendshipRequest(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    FriendshipRequest friendshipRequest=(FriendshipRequest) userRepository.findById(fromUserId).get().getSentFriendshipRequests().toArray()[0];
    log.info("요청 되긴 했나요?:"+friendshipRequest.toString());

    // when
    friendshipRequestService.rejectFriendshipRequest(toUserId, fromUserIdentifier);

    // then
    User toUser=userRepository.findById(toUserId).get();
    User fromUser=userRepository.findById(fromUserId).get();

    log.info("갱신된 요청: "+toUser.getReceivedFriendshipRequests());
    log.info("갱신된 요청: "+fromUser.getSentFriendshipRequests());

    FriendshipRequest sentFriendshipRequest=(FriendshipRequest) fromUser.getSentFriendshipRequests().toArray()[0];
    FriendshipRequest receivedFriendshipRequest=(FriendshipRequest) toUser.getReceivedFriendshipRequests().toArray()[0];

    assertEquals(FriendshipRequestStatus.REJECTED, sentFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(FriendshipRequestStatus.REJECTED, receivedFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(0, toUser.getFriendships().size());
    assertEquals(0, fromUser.getFriendships().size());
  }

  // 친구 요청 취소
  @DisplayName("요청 취소(A-B)")
  @Order(4)
  @Test
  public void CancelFriendshipRequest(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    FriendshipRequest friendshipRequest=(FriendshipRequest) userRepository.findById(fromUserId).get().getSentFriendshipRequests().toArray()[0];
    log.info("요청 되긴 했나요?:"+friendshipRequest.toString());

    // when
    friendshipRequestService.cancelFriendshipRequest(toUserId, fromUserIdentifier);

    // then
    User toUser=userRepository.findById(toUserId).get();
    User fromUser=userRepository.findById(fromUserId).get();

    log.info("갱신된 요청: "+toUser.getReceivedFriendshipRequests());
    log.info("갱신된 요청: "+fromUser.getSentFriendshipRequests());

    FriendshipRequest sentFriendshipRequest=(FriendshipRequest) fromUser.getSentFriendshipRequests().toArray()[0];
    FriendshipRequest receivedFriendshipRequest=(FriendshipRequest) toUser.getReceivedFriendshipRequests().toArray()[0];

    assertEquals(FriendshipRequestStatus.CANCELED, sentFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(FriendshipRequestStatus.CANCELED, receivedFriendshipRequest.getFriendshipRequestStatus());
    assertEquals(0, toUser.getFriendships().size());
    assertEquals(0, fromUser.getFriendships().size());
  }

  // 재요청 테스트
  @DisplayName("친구 요청 취소 처리 후 재요청 테스트")
  @Order(5)
  @Test
  public void RetryFriendshipRequest(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    friendshipRequestService.cancelFriendshipRequest(toUserId, fromUserIdentifier);

    // when
    // when request는 a to b, b to a로 저장 되어야 역요청이 성립하지 않음
    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    // then
    User fromUser=userRepository.findById(fromUserId).get();
    User toUser=userRepository.findById(toUserId).get();
    log.info("갱신된 choco 정보:"+fromUser);
    log.info("갱신된 cheese 정보:"+toUser);

    // 진행중인 송신 요청 뽑기
    List<SentFriendshipRequestResponseDto> friendshipRequests=friendshipRequestService.sentFriendshipRequest(fromUserId);
    SentFriendshipRequestResponseDto sentFriendshipRequestDto=friendshipRequests.get(0);

    // 진행중진 수신 요청 뽑기
    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequests=friendshipRequestService.receivedFriendshipRequest(fromUserId);

    assertNotNull(sentFriendshipRequestDto);
    assertEquals(fromUserIdentifier, sentFriendshipRequestDto.getFriendshipRequestSenderIdentifier());
    assertEquals(toUserIdentifier, sentFriendshipRequestDto.getFriendshipRequestReceiverIdentifier());
    assertEquals(0, receivedFriendshipRequests.size());
  }

  // 예외

  // 친구 추가 요청을 수락하려니 상대방이 이미 취소했다면?
  @DisplayName("수락 시, 이미 취소된 친구 요청(A-B)")
  @Order(6)
  @Test
  public void AlreadyCanceledFriendshipRequestWhenAccept(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    FriendshipRequest friendshipRequest=(FriendshipRequest) userRepository.findById(fromUserId).get().getSentFriendshipRequests().toArray()[0];
    log.info("요청 되긴 했나요?:"+friendshipRequest.toString());

    friendshipRequestService.cancelFriendshipRequest(toUserId, fromUserIdentifier);

    // when

    // then
    assertThrows(AlreadyCanceledFriendshipRequestException.class, ()
    -> friendshipRequestService.acceptFriendshipRequest(toUserId, fromUserIdentifier));

  }

  // 친구 추가 요청을 거절하려니 상대방이 이미 취소했다면?
  @DisplayName("거절 시, 이미 취소된 친구 요청(A-B)")
  @Order(6)
  @Test
  public void AlreadyCanceledFriendshipRequestWhenReject(){
    // given
    Long toUserId=1L;
    Long fromUserId=2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();

    friendshipRequestService.createFriendshipRequest(fromUserId, createToFriendshipRequestDto);

    FriendshipRequest friendshipRequest=(FriendshipRequest) userRepository.findById(fromUserId).get().getSentFriendshipRequests().toArray()[0];
    log.info("요청 되긴 했나요?:"+friendshipRequest.toString());

    friendshipRequestService.cancelFriendshipRequest(toUserId, fromUserIdentifier);

    // when

    // then
    assertThrows(AlreadyCanceledFriendshipRequestException.class, ()
        -> friendshipRequestService.rejectFriendshipRequest(toUserId, fromUserIdentifier));

  }

}