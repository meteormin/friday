package com.meteormin.friday.api.hosts.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meteormin.friday.common.pagination.SimplePage;
import com.meteormin.friday.hosts.domain.Host;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static com.meteormin.friday.api.hosts.resource.HostResources.HostResource;

/**
 * DTO for {@link Host}
 */
@EqualsAndHashCode(callSuper = true)
public class HostResources extends SimplePage<HostResource> {
    @JsonProperty("hosts")
    private transient List<HostResource> hosts;

    /**
     * Host Resources Constructor
     * 
     * @param domain the page of domain
     */
    public HostResources(Page<Host> domain) {
        super(domain.getContent()
                .stream()
                .map(HostResource::fromDomain)
                .toList(),
                domain.getTotalElements(),
                domain.getPageable(),
                "hosts");
    }

    @Builder
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
                    .host(host.getHostname())
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
