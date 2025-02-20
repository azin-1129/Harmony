package com.harmony.controller;

import com.harmony.dto.request.CreateToFriendshipRequestDto;
import com.harmony.dto.request.UpdateFriendshipRequestStatusRequestDto;
import com.harmony.dto.response.ReceivedFriendshipRequestResponseDto;
import com.harmony.dto.response.SentFriendshipRequestResponseDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.FriendshipRequestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value="/friendship-request")
@RequiredArgsConstructor
@RestController
public class FriendshipRequestController {
  private final FriendshipRequestService friendshipRequestService;
  private final Long givenUserId=1L; // 임시 userId

  // 친구 추가 요청
  @PostMapping("/send")
  public ResponseEntity<Object> createFriendshipRequest(
      @RequestBody CreateToFriendshipRequestDto createToFriendshipRequestDto){
    friendshipRequestService.createFriendshipRequest(givenUserId, createToFriendshipRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_CREATE_SUCCESS
    );
  }

  // 보낸 요청 목록
  @GetMapping("/sent")
  public ResponseEntity<Object> sentFriendshipRequest(){
    List<SentFriendshipRequestResponseDto> sentFriendshipRequests=friendshipRequestService.sentFriendshipRequest(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.SENT_FRIENDSHIP_REQUESTS_READ_SUCCESS,
        sentFriendshipRequests
    );
  }

  // 받은 요청 목록
  @GetMapping("/received")
  public ResponseEntity<Object> receivedFriendshipRequest(){
    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequests=friendshipRequestService.receivedFriendshipRequest(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.RECEIVED_FRIENDSHIP_REQUESTS_READ_SUCCESS,
        receivedFriendshipRequests
    );
  }

  // 친구 수락
  @PostMapping("/accept")
  public ResponseEntity<Object> acceptFriendshipRequest(
      @RequestBody UpdateFriendshipRequestStatusRequestDto updateFriendshipRequestStatusRequestDto
  ){
    friendshipRequestService.acceptFriendshipRequest(givenUserId,
        updateFriendshipRequestStatusRequestDto.getSenderIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_ACCEPT_SUCCESS
    );
  }

  // 친구 거절
  @PostMapping("/reject")
  public ResponseEntity<Object> rejectFriendshipRequest(
      @RequestBody UpdateFriendshipRequestStatusRequestDto updateFriendshipRequestStatusRequestDto
  ){
    friendshipRequestService.rejectFriendshipRequest(givenUserId,
        updateFriendshipRequestStatusRequestDto.getSenderIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_REJECT_SUCCESS
    );
  }

  // 친구 요청 취소
  @PostMapping("/cancel")
  public ResponseEntity<Object> cancelFriendshipRequest(
      @RequestBody UpdateFriendshipRequestStatusRequestDto updateFriendshipRequestStatusRequestDto
  ){
    friendshipRequestService.rejectFriendshipRequest(givenUserId,
        updateFriendshipRequestStatusRequestDto.getSenderIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_CANCEL_SUCCESS
    );
  }
}
