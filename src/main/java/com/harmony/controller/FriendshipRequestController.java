package com.harmony.controller;

import com.harmony.dto.request.CancelSentFriendshipRequestRequestDto;
import com.harmony.dto.request.CreateFriendshipRequestDto;
import com.harmony.dto.request.UpdateReceivedFriendshipRequestStatusRequestDto;
import com.harmony.dto.response.ReceivedFriendshipRequestResponseDto;
import com.harmony.dto.response.SentFriendshipRequestResponseDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.FriendshipRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.PathVariable;
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
  @Operation(summary="친구 추가 요청 생성", description="친구 추가 요청을 생성합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="친구 추가 요청에 성공했습니다.")
  })
  public ResponseEntity<Object> createFriendshipRequest(
      @RequestBody CreateFriendshipRequestDto createToFriendshipRequestDto){
    friendshipRequestService.createFriendshipRequest(givenUserId, createToFriendshipRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_CREATE_SUCCESS
    );
  }

  // 친구 추가 요청
  @PostMapping("/send/{senderId}")
  @Operation(summary="테스트용 친구 추가 요청 생성", description="친구 추가 요청을 생성합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="친구 추가 요청에 성공했습니다."),
      @ApiResponse(responseCode="400", description="이미 진행중인 친구 요청입니다.")
  })
  public ResponseEntity<Object> createFriendshipRequest(
      @PathVariable Long senderId,
      @RequestBody CreateFriendshipRequestDto createToFriendshipRequestDto){
    friendshipRequestService.createFriendshipRequest(senderId, createToFriendshipRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_CREATE_SUCCESS
    );
  }

  // 보낸 요청 목록
  @GetMapping("/sent")
  @Operation(summary="보낸 친구 추가 요청 목록", description = "보낸 친구 추가 요청 목록을 조회합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description = "보낸 친구 요청 목록 조회에 성공했습니다.")
  })
  public ResponseEntity<Object> sentFriendshipRequest(){
    List<SentFriendshipRequestResponseDto> sentFriendshipRequests=friendshipRequestService.sentFriendshipRequest(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.SENT_FRIENDSHIP_REQUESTS_READ_SUCCESS,
        sentFriendshipRequests
    );
  }

  // 받은 요청 목록
  @GetMapping("/received")
  @Operation(summary="받은 친구 추가 요청 목록", description = "받은 친구 추가 요청 목록을 조회합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description = "받은 친구 요청 목록 조회에 성공했습니다.")
  })
  public ResponseEntity<Object> receivedFriendshipRequest(){
    List<ReceivedFriendshipRequestResponseDto> receivedFriendshipRequests=friendshipRequestService.receivedFriendshipRequest(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.RECEIVED_FRIENDSHIP_REQUESTS_READ_SUCCESS,
        receivedFriendshipRequests
    );
  }

  // 친구 수락
  @PostMapping("/accept")
  @Operation(summary="받은 친구 추가 요청 수락", description = "받은 친구 추가 요청 목록을 수락합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description = "친구 요청을 수락했습니다."),
      @ApiResponse(responseCode="400", description="이미 취소된 친구 요청입니다.")
  })
  public ResponseEntity<Object> acceptFriendshipRequest(
      @RequestBody UpdateReceivedFriendshipRequestStatusRequestDto updateFriendshipRequestStatusRequestDto
  ){
    friendshipRequestService.acceptFriendshipRequest(givenUserId,
        updateFriendshipRequestStatusRequestDto.getSenderIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_ACCEPT_SUCCESS
    );
  }

  // 친구 거절
  @PostMapping("/reject")
  @Operation(summary="받은 친구 추가 요청 거절", description = "받은 친구 추가 요청 목록을 거절합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description = "친구 요청을 거절했습니다."),
      @ApiResponse(responseCode="400", description="이미 취소된 친구 요청입니다.")
  })
  public ResponseEntity<Object> rejectFriendshipRequest(
      @RequestBody UpdateReceivedFriendshipRequestStatusRequestDto updateFriendshipRequestStatusRequestDto
  ){
    friendshipRequestService.rejectFriendshipRequest(givenUserId,
        updateFriendshipRequestStatusRequestDto.getSenderIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_REJECT_SUCCESS
    );
  }

  // 친구 요청 취소
  @PostMapping("/cancel")
  @Operation(summary="보낸 친구 추가 요청 취소", description = "보낸 친구 추가 요청을 취소합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description = "친구 요청을 취소했습니다.")
  })
  public ResponseEntity<Object> cancelFriendshipRequest(
      @RequestBody CancelSentFriendshipRequestRequestDto cancelSentFriendshipRequestRequestDto
  ){
    friendshipRequestService.cancelFriendshipRequest(givenUserId,
        cancelSentFriendshipRequestRequestDto.getReceiverIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_REQUEST_CANCEL_SUCCESS
    );
  }
}
