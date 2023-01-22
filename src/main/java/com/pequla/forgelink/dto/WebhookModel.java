package com.pequla.forgelink.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WebhookModel {
    private String username;
    private String avatar_url;
    private String content;
}
