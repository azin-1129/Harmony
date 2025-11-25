package com.harmony.service;

import com.harmony.dto.response.SelectBlockResponseDto;
import com.harmony.entity.Block;
import com.harmony.entity.BlockId;
import com.harmony.entity.FriendshipRequestStatus;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.BlockRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class BlockService {
  private final UserService userService;
  private final FriendshipService friendshipService;
  private final FriendshipRequestService friendshipRequestService;
  private final BlockRepository blockRepository;

  // 차단하기
  public void blockUser(Long blockingId, String blockedUserIdentifier){
    User blockingUser=userService.getUserById(blockingId);
    User blockedUser=userService.getUserByUserIdentifier(blockedUserIdentifier);

    Long blockedId=blockedUser.getUserId();
    if(friendshipService.existsFriendDSL(blockingId, blockedId)){
      friendshipService.deleteFriendship(blockingId, blockedUserIdentifier);
    }else {
      // from id가 나로 매핑되어 있는 친구 요청을 가져온다.
      // 내가 보냈는가? (sender=나, SENT) : cancel
      if(friendshipRequestService.existsFriendshipRequestDSL(blockingId, blockedId, FriendshipRequestStatus.SENT)){
        friendshipRequestService.cancelFriendshipRequest(blockingId, blockedUserIdentifier);
      }
      // 내가 받았는가? (receiver=나, RECEIVED) : reject
      else if(friendshipRequestService.existsFriendshipRequestDSL(blockingId, blockedId, FriendshipRequestStatus.RECEIVED)) {
        friendshipRequestService.rejectFriendshipRequest(blockingId, blockedUserIdentifier);
      }
    }

    // 차단 정보 저장
    BlockId blockId=BlockId.builder()
        .blockingUserId(blockingId)
        .blockedUserId(blockedUser.getUserId())
        .build();

    Block block= Block.builder()
      .blockId(blockId)
      .blockingUser(blockingUser)
      .blockedUser(blockedUser)
      .build();

    blockRepository.save(block);
  }

  // 차단한 목록
  public List<SelectBlockResponseDto> selectBlocks(Long userId){
    List<Block> blocks=blockRepository.findByBlockingId(userId);

    List<SelectBlockResponseDto> selectBlockResponseDtos=new ArrayList<>();
    for(Block block:blocks){
      SelectBlockResponseDto selectBlockResponseDto= SelectBlockResponseDto.builder()
          .userIdentifier(block.getBlockedUser().getUserIdentifier())
          .profileImageName(block.getBlockedUser().getProfileImageName())
          .nickname(block.getBlockedUser().getNickname())
          .build();
      selectBlockResponseDtos.add(selectBlockResponseDto);
    }

    return selectBlockResponseDtos;
  }

  // 차단해제 (친구 추가는 알아서 하도록 한다.)
  public String unBlockUser(Long unBlockingId, String unBlockedUserIdentifier){
    User unBlockedUser=userService.getUserByUserIdentifier(unBlockedUserIdentifier);

    Long unBlockedId=unBlockedUser.getUserId();
    Block block=blockRepository.findByBlockingIdAndBlockedId(unBlockingId, unBlockedId)
        .orElseThrow(
            () -> new EntityNotFoundException(
                ErrorCode.BLOCK_NOT_FOUND
            )
        );

    blockRepository.delete(block);
    log.info("차단을 해제했어요.");

    return unBlockedUserIdentifier;
  }
}
