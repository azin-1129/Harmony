package com.harmony.controller;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.GroupChatRoomCreateRequestDto;
import com.harmony.dto.request.PersonalChatRoomCreateRequestDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value="/chatroom")
@RequiredArgsConstructor
@RestController
public class ChatRoomController {
  private final ChatRoomService chatRoomService;

  // 채팅방 생성
  // 개인
  @PostMapping("/create/personal")
  public ResponseEntity<Object> createPersonalChatRoom(@RequestBody @Valid
      PersonalChatRoomCreateRequestDto personalChatRoomCreateRequestDto){
    chatRoomService.createPersonalChatRoom(personalChatRoomCreateRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.PERSONAL_CHATROOM_CREATE_SUCCESS
    );
  }

  // 단체
  @PostMapping("/create/group")
  public ResponseEntity<Object> createGroupChatRoom(@RequestBody @Valid
  GroupChatRoomCreateRequestDto groupChatRoomCreateRequestDto){
    chatRoomService.createGroupChatRoom(groupChatRoomCreateRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.GROUP_CHATROOM_CREATE_SUCCESS
    );
  }

  // 채팅방 조회(보류)

  // 채팅방 변경
  // 채팅방 이름
  @PostMapping("/update")
  public ResponseEntity<Object> createGroupChatRoom(@RequestBody @Valid
  ChatRoomNameForm chatRoomNameForm){
    chatRoomService.updateChatRoomName(chatRoomNameForm);

    return SuccessResponse.createSuccess(
        SuccessCode.CHATROOM_NAME_UPDATE_SUCCESS
    );
  }

  // 채팅방 삭제
  @DeleteMapping("/{chatRoomId}")
  public ResponseEntity<Object> deleteChatRoom(@PathVariable Long chatRoomId){
    chatRoomService.deleteChatRoom(chatRoomId);

    return SuccessResponse.createSuccess(
        SuccessCode.CHATROOM_DELETE_SUCCESS
    );
  }
}
