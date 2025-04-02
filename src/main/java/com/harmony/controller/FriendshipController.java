package com.harmony.controller;

import com.harmony.dto.request.DeleteFriendshipRequestDto;
import com.harmony.dto.response.SelectFriendshipResponseDto;
import com.harmony.global.response.code.SuccessCode;
import com.harmony.global.response.structure.SuccessResponse;
import com.harmony.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value="/friendship")
@RequiredArgsConstructor
@RestController
public class FriendshipController {
  private final FriendshipService friendshipService;
  private final Long givenUserId=1L;

  //  테스트
  @GetMapping("/exist-friend-dsl")
  public ResponseEntity<Object> existsFriendship1(){
    long beforeTime=System.currentTimeMillis();
    friendshipService.existsFriendDSL(givenUserId, 2L);
    long afterTime=System.currentTimeMillis();
    log.info("소요 시간:"+(afterTime-beforeTime));
    return ResponseEntity.ok("테스트 API 호출");
  }

  //  테스트
  @GetMapping("/exist-friend2")
  public ResponseEntity<Object> existsFriendship2(){
    long beforeTime=System.currentTimeMillis();
    friendshipService.existsFriend(givenUserId, 2L);
    long afterTime=System.currentTimeMillis();
    log.info("소요 시간:"+(afterTime-beforeTime));
    return ResponseEntity.ok("테스트 API 호출");
  }

  // 친구 목록
  @GetMapping("/friendships")
  @Operation(summary="추가된 친구 목록", description="현재 추가된 친구 목록을 불러옵니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode = "200", description = "추가된 친구 목록 조회에 성공했습니다.")
  })
  public ResponseEntity<Object> selectFriendships(){
    List<SelectFriendshipResponseDto> friendshipResponseDtos=friendshipService.selectFriendships(givenUserId);

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_READ_SUCCESS,
        friendshipResponseDtos
    );
  }

  // 친구 검색

  // 친구 삭제
  @DeleteMapping
  @Operation(summary="친구 삭제", description="친구를 삭제합니다.")
  @ApiResponses(value={
      @ApiResponse(responseCode="200", description="친구를 삭제했습니다."),
      @ApiResponse(responseCode="400", description="존재하지 않는 친구입니다.")
  })
  public ResponseEntity<Object> deleteFriendship(@RequestBody DeleteFriendshipRequestDto deleteFriendshipRequestDto){
    friendshipService.deleteFriendship(givenUserId, deleteFriendshipRequestDto.getFriendIdentifier());

    return SuccessResponse.createSuccess(
        SuccessCode.FRIENDSHIP_DELETE_SUCCESS
    );
  }
}
