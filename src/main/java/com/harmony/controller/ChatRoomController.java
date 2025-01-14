package com.harmony.controller;

import com.harmony.dto.form.ChatRoomNameForm;
import com.harmony.dto.request.GroupChatRoomCreateRequestDto;
import com.harmony.dto.request.PersonalChatRoomCreateRequestDto;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.ErrorResponse;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

  // TODO: bindingResult 메소드 추가?

  // 채팅방 생성
  // 개인
  @PostMapping("/create/personal")
  @Operation(summary="개인 채팅방 생성", description="개인 채팅방 생성 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="개인 채팅방 생성 성공")
  })
  public ResponseEntity<Object> createPersonalChatRoom(@RequestBody
      PersonalChatRoomCreateRequestDto personalChatRoomCreateRequestDto) {
    chatRoomService.createPersonalChatRoom(personalChatRoomCreateRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.PERSONAL_CHATROOM_CREATE_SUCCESS
    );
  }

  // 단체
  @PostMapping("/create/group")
  @Operation(summary="단체 채팅방 생성", description="단체 채팅방 생성 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="단체 채팅방 생성 성공")
  })
  public ResponseEntity<Object> createGroupChatRoom(@RequestBody @Valid
  GroupChatRoomCreateRequestDto groupChatRoomCreateRequestDto, BindingResult bindingResult) {
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

    chatRoomService.createGroupChatRoom(groupChatRoomCreateRequestDto);

    return SuccessResponse.createSuccess(
        SuccessCode.GROUP_CHATROOM_CREATE_SUCCESS
    );
  }

  // 채팅방 조회(보류)

  // 채팅방 변경
  // 채팅방 이름
  @PostMapping("/update")
  @Operation(summary="채팅방 이름 변경", description="채팅방 이름 변경 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="채팅방 이름 변경 성공")
  })
  public ResponseEntity<Object> updateChatRoomName(@RequestBody @Valid
  ChatRoomNameForm chatRoomNameForm, BindingResult bindingResult) {
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

    chatRoomService.updateChatRoomName(chatRoomNameForm);

    return SuccessResponse.createSuccess(
        SuccessCode.CHATROOM_NAME_UPDATE_SUCCESS
    );
  }

  // 채팅방 삭제
  @DeleteMapping("/{chatRoomId}")
  @PostMapping("/update")
  @Operation(summary="채팅방 삭제", description="채팅방 삭제 API")
  @Parameters({
      @Parameter(name="chatRoomId", description="채팅방ID", example="1")
  })
  @ApiResponses(value={
      @ApiResponse(responseCode="201", description="채팅방 삭제 성공"),
      @ApiResponse(responseCode="404", description="삭제하려는 채팅방이 존재하지 않습니다.")
  })
  public ResponseEntity<Object> deleteChatRoom(@PathVariable Long chatRoomId){
    chatRoomService.deleteChatRoom(chatRoomId);

    return SuccessResponse.createSuccess(
        SuccessCode.CHATROOM_DELETE_SUCCESS
    );
  }
}
