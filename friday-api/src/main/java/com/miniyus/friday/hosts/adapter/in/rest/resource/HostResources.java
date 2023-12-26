package com.miniyus.friday.hosts.adapter.in.rest.resource;

import com.miniyus.friday.common.pagination.SimplePage;
import com.miniyus.friday.hosts.domain.Host;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static com.miniyus.friday.hosts.adapter.in.rest.resource.HostResources.HostResource;

/**
 * DTO for {@link Host}
 */
@EqualsAndHashCode(callSuper = true)
@Schema(name = "HostResources")
public class HostResources extends SimplePage<HostResource> implements Serializable {
    @Schema(name = "hosts", description = "hosts")
    private transient List<HostResource> hosts;

    /**
     * Host Resources Constructor
     * @param domain the page of domain
     */
    public HostResources(Page<Host> domain) {
        super(
            domain.getContent()
                .stream()
                .map(HostResource::fromDomain)
                .toList(),
            domain.getTotalElements(),
            domain.getPageable(),
            "hosts");
    }

    @Builder
    @Schema(name = "HostResource")
    public record HostResource(
        Long id,
        String host,
        String summary,
        String description,
        String path,
        boolean publish,
        LocalDateTime createdAt,
        LocalDateTime updatedAt)
        implements Serializable {
        public static HostResource fromDomain(Host host) {
            return HostResource.builder()
                .id(host.getId())
                .host(host.getHost())
                .summary(host.getSummary())
                .description(host.getDescription())
                .path(host.getPath())
                .publish(host.isPublish())
                .createdAt(host.getCreatedAt())
                .updatedAt(host.getUpdatedAt())
                .build();
        }
    }
}
