package com.harmony.service;

import com.harmony.dto.request.CreateFriendshipRequestDto;
import com.harmony.dto.response.ReceivedFriendshipRequestResponseDto;
import com.harmony.dto.response.SentFriendshipRequestResponseDto;
import com.harmony.entity.FriendType;
import com.harmony.entity.Friendship;
import com.harmony.entity.FriendshipRequest;
import com.harmony.entity.FriendshipRequestStatus;
import com.harmony.entity.User;
import com.harmony.exception.AlreadyCanceledFriendshipRequestException;
import com.harmony.global.response.code.ErrorCode;import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.FriendshipRepository;
import com.harmony.repository.FriendshipRequestRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FriendshipRequestService {
  private final UserService userService;
  private final FriendshipRequestRepository friendshipRequestRepository;
  private final FriendshipRepository friendshipRepository;

  // TODO: 친구 삭제한 이력이 있다면 그때는..? PENDING인 애들만 골라야겠지.
  public void createFriendshipRequest(Long fromUserId, CreateFriendshipRequestDto createToFriendshipRequestDto){
    // from user의 identifier 뽑아내기
    // to user의 identifier?
    String receiverIdentifier=createToFriendshipRequestDto.getReceiverIdentifier();

    User sender =userService.getUserById(fromUserId);
    User receiver =userService.getUserByUserIdentifier(receiverIdentifier);

    System.out.println("유저 정보를 불러 왔습니다.");

    // 양쪽 요청 저장하기
    FriendshipRequest tofriendshipRequest= FriendshipRequest.builder()
        .friendshipRequestSender(sender)
        .friendshipRequestReceiver(receiver)
        .friendshipRequestStatus(FriendshipRequestStatus.PENDING)
        .build();

    FriendshipRequest fromFriendshipRequest= FriendshipRequest.builder()
        .friendshipRequestSender(receiver)
        .friendshipRequestReceiver(sender)
        .friendshipRequestStatus(FriendshipRequestStatus.PENDING)
        .build();

    System.out.println("양쪽 요청 엔티티를 생성했습니다.");

    friendshipRequestRepository.save(tofriendshipRequest);
    friendshipRequestRepository.save(fromFriendshipRequest);

    System.out.println("양쪽 요청 엔티티의 id를 생성했습니다.");
    // 친구 요청 상황 저장
    tofriendshipRequest.setFriendshipRequestInfo(sender, receiver);
    fromFriendshipRequest.setFriendshipRequestInfo(receiver, sender);
    System.out.println("친구 요청 상황을 저장했습니다.");
  }

  // 내가 보낸 요청
  public List<SentFriendshipRequestResponseDto> sentFriendshipRequest(Long userId){
    User user=userService.getUserById(userId);
    // 진행중인 요청 뽑기
    Iterator<FriendshipRequest> iter=user.getSentFriendshipRequests().iterator();

    List<SentFriendshipRequestResponseDto> sentFriendshipRequestResponseDtos=new ArrayList<SentFriendshipRequestResponseDto>();

    // 내가 보낸 입장이며, 진행중인 친구 요청이라면 sentFriendshipRequests에 추가
    while(iter.hasNext()){
      FriendshipRequest sentFriendshipRequest=iter.next();
      if(sentFriendshipRequest.getFriendshipRequestStatus().equals(FriendshipRequestStatus.PENDING)
      & sentFriendshipRequest.getFriendshipRequestSender().getUserId().equals(userId)){
        SentFriendshipRequestResponseDto sentFriendshipRequestDto=SentFriendshipRequestResponseDto.builder()
                .friendshipRequestSenderIdentifier(sentFriendshipRequest.getFriendshipRequestSender().getUserIdentifier())
                .friendshipRequestReceiverIdentifier(sentFriendshipRequest.getFriendshipRequestReceiver().getUserIdentifier())
                .build();
        sentFriendshipRequestResponseDtos.add(sentFriendshipRequestDto);

        System.out.println("현재 진행중인 send 요청입니다: "+sentFriendshipRequestDto);
      }
    }

    return sentFriendshipRequestResponseDtos;
  }

  // 내가 받은 요청
  public List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequest(Long userId){
    User user=userService.getUserById(userId);
    // 진행중인 요청 뽑기
    Iterator<FriendshipRequest> iter=user.getSentFriendshipRequests().iterator();

    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequests=new ArrayList<ReceivedFriendshipRequestResponseDto>();

    // 내가 보낸 입장이며, 진행중인 친구 요청이라면 receivedFriendshipRequests에 추가
    while(iter.hasNext()){
      FriendshipRequest receivedFriendshipRequest=iter.next();
      if(receivedFriendshipRequest.getFriendshipRequestStatus().equals(FriendshipRequestStatus.PENDING)
          & receivedFriendshipRequest.getFriendshipRequestReceiver().getUserId().equals(userId)){
        ReceivedFriendshipRequestResponseDto receivedFriendshipRequestDto=ReceivedFriendshipRequestResponseDto.builder()
            .friendshipRequestSenderIdentifier(receivedFriendshipRequest.getFriendshipRequestSender().getUserIdentifier())
            .friendshipRequestReceiverIdentifier(receivedFriendshipRequest.getFriendshipRequestReceiver().getUserIdentifier())
            .build();
        receivedFriendshipRequests.add(receivedFriendshipRequestDto);
        System.out.println("현재 진행중인 send 요청입니다: "+receivedFriendshipRequestDto);
      }
    }

    return receivedFriendshipRequests;
  }

  public void acceptFriendshipRequest(Long receiverId, String senderIdentifier){
    User receiver =userService.getUserById(receiverId); // 요청을 받은 유저
    User sender =userService.getUserByUserIdentifier(senderIdentifier);

    System.out.println("유저 정보를 불러왔습니다.");

    FriendshipRequest toFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        receiverId, sender.getUserId(), FriendshipRequestStatus.PENDING);
    FriendshipRequest fromFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        sender.getUserId(), receiverId, FriendshipRequestStatus.PENDING);

    // 유효한 요청이 없다면
    if(toFriendshipRequest==null){
      throw new AlreadyCanceledFriendshipRequestException(
          ErrorCode.FRIENDSHIP_REQUEST_ALREADY_CANCELED
      );
    }

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.ACCEPTED);
    fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.ACCEPTED);

    System.out.println("요청 정보의 상태를 갱신했습니다.");

    // friend에 Insert
    Friendship friendshipTo=Friendship.builder()
        .user(receiver)
        .friend(sender)
        .friendType(FriendType.FRIEND)
        .build();

    Friendship friendshipFrom=Friendship.builder()
        .user(sender)
        .friend(receiver)
        .friendType(FriendType.FRIEND)
        .build();

    System.out.println("친구 엔티티를 생성했습니다.");

    friendshipRepository.save(friendshipTo);
    friendshipRepository.save(friendshipFrom);

    System.out.println("친구 엔티티의 id를 생성했습니다.");

    friendshipTo.setUsers(receiver, sender);
    friendshipFrom.setUsers(sender, receiver);

    System.out.println("친구 엔티티를 추가했습니다.");

    System.out.println("친구가 됐어용."+friendshipTo);
    System.out.println("친구가 됐어용."+friendshipFrom);
  }

  public void rejectFriendshipRequest(Long receiverId, String senderIdentifier){
    User sender =userService.getUserByUserIdentifier(senderIdentifier);
    User receiver =userService.getUserById(receiverId); // 요청을 받은 유저

    System.out.println("기존 요청:"+ receiver.getReceivedFriendshipRequests());
    System.out.println("기존 요청:"+ sender.getSentFriendshipRequests());

    // 진행중인 요청의 상태를 REJECTED로 변경
    FriendshipRequest toFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        receiverId, sender.getUserId(), FriendshipRequestStatus.PENDING);
    FriendshipRequest fromFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        sender.getUserId(),
        receiverId, FriendshipRequestStatus.PENDING);

    if(toFriendshipRequest==null){
      throw new AlreadyCanceledFriendshipRequestException(
          ErrorCode.FRIENDSHIP_REQUEST_ALREADY_CANCELED
      );
    }

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.REJECTED);
    fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.REJECTED);

    System.out.println("친구 요청을 거절했습니다.");
  }

  public void cancelFriendshipRequest(Long receiverId, String senderIdentifier){
    User sender =userService.getUserByUserIdentifier(senderIdentifier);
    User receiver =userService.getUserById(receiverId); // 요청을 받은 유저

    System.out.println("기존 요청:"+ receiver.getReceivedFriendshipRequests());
    System.out.println("기존 요청:"+ sender.getSentFriendshipRequests());

    // 진행중인 요청의 상태를 CANCELED로 변경
    FriendshipRequest toFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        receiverId, sender.getUserId(), FriendshipRequestStatus.PENDING);
    FriendshipRequest fromFriendshipRequest=friendshipRequestRepository.findFriendshipRequest(
        sender.getUserId(),
        receiverId, FriendshipRequestStatus.PENDING);

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    toFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
    fromFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

    System.out.println("친구 요청을 취소했습니다.");
  }
}
