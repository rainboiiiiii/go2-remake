package com.go2super.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayUserDTO {
    private String url;
    private String gameUrl;
    private String sessionKey;
    private long userId;
}
