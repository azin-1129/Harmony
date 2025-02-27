package com.harmony.service;

import com.harmony.dto.response.FriendshipResponseDto;
import com.harmony.entity.Friendship;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.repository.FriendshipRepository;
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
public class FriendshipService {
  private final FriendshipRepository friendshipRepository;
  private final UserService userService;

  // TODO: 페이징 처리, 프론트에서 친구 검색
  public List<FriendshipResponseDto> selectFriendships(Long userId){
    List<Friendship> friendships=friendshipRepository.findByUserId(userId);

    List<FriendshipResponseDto> friendshipResponseDtos=new ArrayList<>();
    for(Friendship friendship:friendships){
      FriendshipResponseDto friendshipResponseDto= FriendshipResponseDto.builder()
          .friendIdentifier(friendship.getFriend().getUserIdentifier())
          .nickname(friendship.getFriend().getNickname())
          .profileImageName(friendship.getFriend().getProfileImageName())
          .friendType(friendship.getFriendType())
          .build();
      friendshipResponseDtos.add(friendshipResponseDto);
    }

    return friendshipResponseDtos;
  }

  public void deleteFriendship(Long userId, String friendIdentifier){
    User user=userService.getUserById(userId);
    User friend=userService.getUserByUserIdentifier(friendIdentifier);

    Optional<Friendship> friendship=friendshipRepository.findByUserIdAndFriendId(userId, friend.getUserId());
    if(friendship.isPresent()){
      user.getFriendships().remove(friendship.get());

      Optional<Friendship> reversedFriendship=friendshipRepository.findByUserIdAndFriendId(friend.getUserId(), userId);
      friend.getFriendships().remove(reversedFriendship.get());

      System.out.println("친구를 삭제했습니다.");
    }else{
      throw new EntityNotFoundException(
          ErrorCode.USER_NOT_FOUND
      );
    }
  }
}
