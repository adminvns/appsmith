package com.appsmith.server.git;

import com.appsmith.server.exceptions.AppsmithError;
import com.appsmith.server.exceptions.AppsmithException;
import com.appsmith.server.helpers.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static com.appsmith.server.helpers.GitUtils.MAX_RETRIES;
import static com.appsmith.server.helpers.GitUtils.RETRY_DELAY;

@Component
@RequiredArgsConstructor
public class GitRedisUtils {

    private final RedisUtils redisUtils;

    public Mono<Boolean> addFileLock(String defaultApplicationId) {
        return redisUtils
                .addFileLock(defaultApplicationId)
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new AppsmithException(AppsmithError.GIT_FILE_IN_USE);
                        }));
    }

    public Mono<Boolean> addFileLockWithoutRetry(String defaultApplicationId) {
        return redisUtils.addFileLock(defaultApplicationId);
    }

    public Mono<Boolean> releaseFileLock(String defaultApplicationId) {
        return redisUtils.releaseFileLock(defaultApplicationId);
    }
}
