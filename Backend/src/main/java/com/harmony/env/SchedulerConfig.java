package com.harmony.env;

import com.harmony.env.util.SchedulerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SchedulerUtil schedulerUtil;

    // 하루마다 새벽에.
    @Scheduled(cron="00 35 16 * * *")
    public void deleteWithdrawnUsersData(){
        schedulerUtil.deleteWithdrawnUsersDataWithoutChatInfo();
    }

    // 삭제 예정인 게시물 하루마다 정리.
    @Scheduled(cron="00 36 14 * * *")
    public void deleteArticlesInfo(){
        schedulerUtil.deleteArticlesInfo();
    }

    // 한 달마다 새벽 3시에. cron=0 0 3 1 * ?
    // 채팅방 참여자 0인 데이터 정리
    @Scheduled(cron="00 30 16 * * *")
    public void deleteZeroCountChatRoomsInfo(){
        schedulerUtil.deleteZeroCountChatRoomsInfo();
    }
}
