package com.harmony.controller;

import com.harmony.dto.request.BlockRequestDto;
import com.harmony.dto.request.UnBlockRequestDto;
import com.harmony.dto.response.SelectBlockResponseDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value="/block")
@RequiredArgsConstructor
@RestController
public class BlockController {
  private final BlockService blockService;
  private final Long givenUserId=1L;

  // 차단하기
  @PostMapping("/blocking")
  @Operation(summary="차단하기", description="차단 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="해당 유저를 차단했습니다.")
  })
  public ResponseEntity<Object> blockUser(@RequestBody BlockRequestDto blockRequestDto){
    blockService.blockUser(givenUserId, blockRequestDto.getBlockedUserIdentifier());
    return SuccessResponse.createSuccess(
        SuccessCode.BLOCK_SUCCESS
    );
  }

  // 차단 목록
  @GetMapping("/block-list")
  @Operation(summary="차단한 상대방의 목록", description="차단 목록 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="차단한 상대 목록 조회에 성공했습니다.")
  })
  public ResponseEntity<Object> selectBlocks(){
    List<SelectBlockResponseDto> blockResponseDtos=blockService.selectBlocks(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.BLOCK_READ_SUCCESS,
        blockResponseDtos
    );
  }
  // 차단해제
  @PostMapping("/unblock")
  @Operation(summary="차단 해제", description="차단 해제 API")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="해당 유저 차단을 해제했습니다."),
      @ApiResponse(responseCode="400", description="존재하지 않는 차단 이력입니다.")
  })
  public ResponseEntity<Object> unBlockUser(@RequestBody UnBlockRequestDto unBlockRequestDto){
    return SuccessResponse.createSuccess(
        SuccessCode.UN_BLOCK_SUCCESS,
        blockService.unBlockUser(givenUserId, unBlockRequestDto.getUnBlockedUserIdentifier())
    );
  }
}
