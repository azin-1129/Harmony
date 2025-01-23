package com.harmony.controller;

import com.harmony.dto.request.CreateAndParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.CreateAndParticipatePersonalChatRoomRequestDto;
import com.harmony.dto.request.LeaveChatRoomForceRequestDto;
import com.harmony.dto.request.LeaveChatRoomRequestDto;
import com.harmony.dto.request.ParticipateGroupChatRoomRequestDto;
import com.harmony.dto.request.SelectChatRoomParticipantsRequestDto;
import com.harmony.dto.response.SelectChatRoomParticipantsResponseDto;
import com.harmony.dto.response.SelectParticipatedChatRoomsResponseDto;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.ErrorResponse;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.ParticipantService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value="/participate")
@RequiredArgsConstructor
@RestController
public class ParticipantController {
  private final ParticipantService participantService;
  private final Long givenUserId=1L; // 임시 userId
  // CREATE
  // 개인 채팅방 참가+생성(방 제목 어쩌지.. null 처리?)
  @PostMapping("/init/personal")
  public ResponseEntity<Object> createAndParticipatePersonalChatRoom(@RequestBody
  CreateAndParticipatePersonalChatRoomRequestDto createAndParticipatePersonalChatRoomRequestDto) {
    Long createdPersonalChatRoomId=participantService.createAndParticipatePersonalChatRoom(givenUserId, createAndParticipatePersonalChatRoomRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.PERSONAL_CHATROOM_CREATE_SUCCESS,
        createdPersonalChatRoomId
    );
  }

  // 단체 채팅방 참가+생성(BindingResult 필요)
  @PostMapping("/init/group")
  public ResponseEntity<Object> createAndParticipateGroupChatRoom(@RequestBody @Valid CreateAndParticipateGroupChatRoomRequestDto createAndParticipateGroupChatRoomRequestDto, BindingResult bindingResult) {
    // 입력 형식이 올바르지 않다면 에러
    if(bindingResult.hasErrors()){
      Map<String, String> validatorResult=new HashMap<>();

      for(FieldError error:bindingResult.getFieldErrors()){
        String validKeyName=String.format("valid_%s",error.getField());
        validatorResult.put(validKeyName, error.getDefaultMessage());
      }

      return ErrorResponse.createError(
          ErrorCode.INVALID_ARGUMENT,
          validatorResult
      );
    }

    Long createdGroupChatRoomId= participantService.createAndParticipateGroupChatRoom(givenUserId, createAndParticipateGroupChatRoomRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.GROUP_CHATROOM_CREATE_SUCCESS,
        createdGroupChatRoomId
    );
  }

  // TODO: 생성+참가 Response Code 분기?
  @PostMapping("/group")
  // 단체 채팅방 참여
  public ResponseEntity<Object> participateGroupChatRoom(@RequestBody ParticipateGroupChatRoomRequestDto participateGroupChatRoomRequestDto) {
    Long participatedGroupChatId= participantService.participateGroupChatRoom(givenUserId, participateGroupChatRoomRequestDto);

    return SuccessResponse.createSuccess(
          SuccessCode.PARTICIPATE_GROUP_CHATROOM_SUCCESS,
          participatedGroupChatId
    );
  }

  // READ
  // 채팅방 참가자 목록 조회
  @PostMapping("/participants")
  public ResponseEntity<Object> selectChatRoomParticipants(@RequestBody
      SelectChatRoomParticipantsRequestDto selectChatRoomParticipantsRequestDto) {
    List<SelectChatRoomParticipantsResponseDto> filteredParticipants=participantService.selectChatRoomParticipants(selectChatRoomParticipantsRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.PARTICIPANT_READ_SUCCESS,
        filteredParticipants
    );
  }

  // 내가 참여한 채팅방 목록 조회
  @GetMapping("/participated")
  public ResponseEntity<Object> selectParticipatedChatRooms(){
    List<SelectParticipatedChatRoomsResponseDto> filteredChatRooms=participantService.selectParticipatedChatRooms(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.PARTICIPATED_CHATROOM_READ_SUCCESS,
        filteredChatRooms
    );
  }

  // DELETE
  // 채팅방 스스로 퇴장
  @DeleteMapping("/leave")
  public ResponseEntity<Object> leaveChatRoom(@RequestBody LeaveChatRoomRequestDto leaveChatRoomRequestDto){
    participantService.leaveChatRoom(givenUserId, leaveChatRoomRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.LEAVE_CHATROOM_SUCCESS
    );
  }

  // 채팅방 강제 퇴장
  @DeleteMapping("/leave-forced")
  public ResponseEntity<Object> leaveChatRoomForce(@RequestBody LeaveChatRoomForceRequestDto leaveChatRoomForceRequestDto){
    participantService.leaveChatRoomForce(leaveChatRoomForceRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.LEAVE_CHATROOM_FORCE_SUCCESS
    );
  }
}
