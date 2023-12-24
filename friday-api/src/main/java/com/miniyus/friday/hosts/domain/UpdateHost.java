package com.miniyus.friday.hosts.domain;

import lombok.Builder;

@Builder
public record UpdateHost(
    HostIds ids,
    String host,
    String summary,
    String description,
    String path,
    boolean publish
) {
}