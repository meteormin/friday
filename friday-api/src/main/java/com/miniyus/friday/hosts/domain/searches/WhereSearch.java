package com.miniyus.friday.hosts.domain.searches;

import lombok.Builder;

@Builder
public record WhereSearch(
    Long hostId,
    String queryKey,
    String query
) {
}
