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
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityAlreadyExistException;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.FriendshipRepository;
import com.harmony.repository.FriendshipRequestRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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

  public void createFriendshipRequest(Long fromUserId, CreateFriendshipRequestDto createToFriendshipRequestDto){
    String receiverIdentifier=createToFriendshipRequestDto.getReceiverIdentifier();

    User receiver =userService.getUserByUserIdentifier(receiverIdentifier);
    Optional<FriendshipRequest> friendshipRequest=friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        fromUserId, receiver.getUserId(), FriendshipRequestStatus.SENT);

    if(friendshipRequest.isPresent()){
      throw new EntityAlreadyExistException(
          ErrorCode.FRIENDSHIP_REQUEST_ALREADY_EXIST
      );
    }

    User sender =userService.getUserById(fromUserId);
    System.out.println("유저 정보를 불러 왔습니다.");

    // 양쪽 요청 저장하기
    FriendshipRequest sendingfriendshipRequest = FriendshipRequest.builder()
        .friendshipRequestSender(sender)
        .friendshipRequestReceiver(receiver)
        .friendshipRequestStatus(FriendshipRequestStatus.SENT)
        .build();

    FriendshipRequest willReceiveFriendshipRequest = FriendshipRequest.builder()
        .friendshipRequestSender(receiver)
        .friendshipRequestReceiver(sender)
        .friendshipRequestStatus(FriendshipRequestStatus.RECEIVED)
        .build();

    System.out.println("양쪽 요청 엔티티를 생성했습니다.");

    friendshipRequestRepository.save(sendingfriendshipRequest);
    friendshipRequestRepository.save(willReceiveFriendshipRequest);

    System.out.println("양쪽 요청 엔티티의 id를 생성했습니다.");
    // 친구 요청 상황 저장
    sendingfriendshipRequest.setFriendshipRequestInfo(sender, receiver);
    willReceiveFriendshipRequest.setFriendshipRequestInfo(receiver, sender);
    System.out.println("친구 요청 상황을 저장했습니다.");
  }

  // 내가 보낸 요청
  public List<SentFriendshipRequestResponseDto> sentFriendshipRequest(Long userId){
    List<FriendshipRequest> sentFriendshipRequests=friendshipRequestRepository.findByFromUserIdAndFriendshipReqStatus(userId, FriendshipRequestStatus.SENT);

    List<SentFriendshipRequestResponseDto> sentFriendshipRequestResponseDtos=new ArrayList<SentFriendshipRequestResponseDto>();
    for(FriendshipRequest sentFriendshipRequest:sentFriendshipRequests){
      SentFriendshipRequestResponseDto sentFriendshipRequestDto=SentFriendshipRequestResponseDto.builder()
          .friendshipRequestSenderIdentifier(sentFriendshipRequest.getFriendshipRequestSender().getUserIdentifier())
          .friendshipRequestReceiverIdentifier(sentFriendshipRequest.getFriendshipRequestReceiver().getUserIdentifier())
          .build();
      sentFriendshipRequestResponseDtos.add(sentFriendshipRequestDto);
    }

    return sentFriendshipRequestResponseDtos;
  }

  // 내가 받은 요청
  public List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequest(Long userId){
    List<FriendshipRequest> receivedFriendshipRequests=friendshipRequestRepository.findByFromUserIdAndFriendshipReqStatus(userId, FriendshipRequestStatus.RECEIVED);

    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequestResponseDtos=new ArrayList<ReceivedFriendshipRequestResponseDto>();
    for(FriendshipRequest receivedFriendshipRequest:receivedFriendshipRequests){
      ReceivedFriendshipRequestResponseDto receivedFriendshipRequestDto=ReceivedFriendshipRequestResponseDto.builder()
          .friendshipRequestReceiverIdentifier(receivedFriendshipRequest.getFriendshipRequestSender().getUserIdentifier())
          .friendshipRequestSenderIdentifier(receivedFriendshipRequest.getFriendshipRequestReceiver().getUserIdentifier())
          .build();
      receivedFriendshipRequestResponseDtos.add(receivedFriendshipRequestDto);
    }

    return receivedFriendshipRequestResponseDtos;
  }

  public void acceptFriendshipRequest(Long receiverId, String senderIdentifier){
    User receiver =userService.getUserById(receiverId); // 요청을 받은 유저
    User sender =userService.getUserByUserIdentifier(senderIdentifier);

    System.out.println("유저 정보를 불러왔습니다.");

    FriendshipRequest graphFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        receiverId, sender.getUserId(), FriendshipRequestStatus.RECEIVED).orElseThrow(
        () -> new AlreadyCanceledFriendshipRequestException(
            ErrorCode.INVALID_FRIENDSHIP_REQUEST
        )
    );

    FriendshipRequest receivedFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        sender.getUserId(), receiverId, FriendshipRequestStatus.SENT).orElseThrow(
        () -> new AlreadyCanceledFriendshipRequestException(
            ErrorCode.INVALID_FRIENDSHIP_REQUEST
        )
    );

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      graphFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    graphFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.ACCEPTED);
    receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.ACCEPTED);

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
    FriendshipRequest receivedFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        receiverId, sender.getUserId(), FriendshipRequestStatus.RECEIVED).orElseThrow(
        () -> new AlreadyCanceledFriendshipRequestException(
            ErrorCode.INVALID_FRIENDSHIP_REQUEST
        )
    );
    FriendshipRequest sentFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        sender.getUserId(),
        receiverId, FriendshipRequestStatus.SENT).orElseThrow(
        () -> new AlreadyCanceledFriendshipRequestException(
            ErrorCode.INVALID_FRIENDSHIP_REQUEST
        )
    );

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      sentFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.REJECTED);
    sentFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.REJECTED);

    System.out.println("친구 요청을 거절했습니다.");
  }

  public void cancelFriendshipRequest(Long receiverId, String senderIdentifier){
    User sender =userService.getUserByUserIdentifier(senderIdentifier);
    User receiver =userService.getUserById(receiverId); // 요청을 받은 유저

    System.out.println("기존 요청:"+ receiver.getReceivedFriendshipRequests());
    System.out.println("기존 요청:"+ sender.getSentFriendshipRequests());

    // 진행중인 요청의 상태를 CANCELED로 변경
    FriendshipRequest receivedFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        receiverId, sender.getUserId(), FriendshipRequestStatus.RECEIVED).get();
    FriendshipRequest sentFriendshipRequest =friendshipRequestRepository.findByToUserIdAndFromUserIdAndFriendshipRequestStatus(
        sender.getUserId(),
        receiverId, FriendshipRequestStatus.SENT).get();

    // 상대방이 탈퇴 처리 진행중이라면
    if(sender.getWithdraw()){
      receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
      sentFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

      throw new EntityNotFoundException(
          ErrorCode.USER_ALREADY_WITHDRAW
      );
    }

    receivedFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);
    sentFriendshipRequest.setFriendshipRequestStatus(FriendshipRequestStatus.CANCELED);

    System.out.println("친구 요청을 취소했습니다.");
  }
}
