package com.harmony.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateAndParticipateGroupChatRoomRequestDto {
  @NotBlank(message="채팅방 이름을 입력하세요.")
  @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]{1,30}$", message = "채팅방 이름은 영문 대소문자,한글, 숫자, 공백 1~30자리여야 합니다.")
  @Schema(description="영문 대소문자,한글, 숫자, 공백 1~30자리 채팅방 이름", defaultValue="마라탕 드실 분")
  private String chatRoomName;

  @NotNull(message="채팅방 최대 입장 인원수를 입력하세요.")
  @Min(value = 3, message = "채팅방 최대 입장 인원수는 최소 3 이상입니다.")
  @Max(value= 1500, message="채팅방 최대 입장 인원수는 최대 1500 입니다.")
  @Schema(description="채팅방 최대 입장 인원수 3~1500", defaultValue = "3")
  private Integer chatRoomCountMax;
}
