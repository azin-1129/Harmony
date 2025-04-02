package com.harmony.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.harmony.dto.request.CreateFriendshipRequestDto;
import com.harmony.dto.response.SelectFriendshipResponseDto;
import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
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
class FriendshipServiceTest {
  @Autowired
  private EntityManager em;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FriendshipService friendshipService;

  @Autowired
  private FriendshipRequestService friendshipRequestService;

  User choco, cheese;

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
  }

  @AfterAll
  void cleanUp(){
    userRepository.deleteAll();
  }

  // 친구 추가 후 조회(A-B)
  @DisplayName("친구 추가 후 조회 테스트(choco&cheese)")
  @Order(1)
  @Test
  public void readFriendship(){
    // given
    Long receiverId =1L;
    Long senderId =2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();
    friendshipRequestService.createFriendshipRequest(senderId, createToFriendshipRequestDto);

    friendshipRequestService.acceptFriendshipRequest(receiverId, fromUserIdentifier);

    // when
    List<SelectFriendshipResponseDto> friendships=friendshipService.selectFriendships(receiverId);

    System.out.println("친구 목록:"+friendships);
    // then
    assertEquals(1, friendships.size());
  }

  // 친구 삭제 (친구 했다가 A,B 관계 끊기)
  @DisplayName("친구 삭제 후 검색 테스트(choco&cheese)")
  @Order(2)
  @Test
  public void deleteFriendship(){
    // given
    Long receiverId =1L;
    Long senderId =2L;
    String toUserIdentifier="choco";
    String fromUserIdentifier="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(toUserIdentifier)
        .build();
    friendshipRequestService.createFriendshipRequest(senderId, createToFriendshipRequestDto);

    friendshipRequestService.acceptFriendshipRequest(receiverId, fromUserIdentifier);

    User receiver=userRepository.findById(receiverId).get();
    System.out.println("친삭 이전:"+receiver.getFriendships());
    // when
    friendshipService.deleteFriendship(receiverId, fromUserIdentifier);

    em.flush();
    System.out.println("친삭 후:"+receiver.getFriendships());
    List<SelectFriendshipResponseDto> friendships=friendshipService.selectFriendships(receiverId);

    // then
    assertEquals(0, friendships.size());
  }

  // 예외

  // 친구 삭제 하려니 상대방이 이미 삭제했다면? - 친구 MVC
  @DisplayName("이미 상대방이 삭제했을 때 삭제 시도 테스트(choco&cheese)")
  @Order(3)
  @Test
  public void alreadyDeletedFriendship(){
    // given
    Long receiverId =1L;
    Long senderId =2L;
    String receiverIdentifier ="choco";
    String senderIdentifier ="cheese";

    CreateFriendshipRequestDto createToFriendshipRequestDto= CreateFriendshipRequestDto.builder()
        .receiverIdentifier(receiverIdentifier)
        .build();
    friendshipRequestService.createFriendshipRequest(senderId, createToFriendshipRequestDto);

    friendshipRequestService.acceptFriendshipRequest(receiverId, senderIdentifier);

    friendshipService.deleteFriendship(receiverId, senderIdentifier);
    // when

    // then
    assertThrows(EntityNotFoundException.class, ()
    -> friendshipService.deleteFriendship(receiverId, senderIdentifier));
  }
}