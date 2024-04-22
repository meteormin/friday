package com.meteormin.friday.api.searches.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meteormin.friday.common.pagination.SimplePage;
import com.meteormin.friday.hosts.domain.searches.Search;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.util.List;

import static com.meteormin.friday.api.searches.resource.SearchResources.SearchResource;

@EqualsAndHashCode(callSuper = true)
public class SearchResources extends SimplePage<SearchResource> {
    @JsonProperty("searches")
    private transient List<SearchResource> searches;

    public SearchResources(Page<Search> domain) {
        super(
                domain.getContent()
                        .stream()
                        .map(SearchResource::fromDomain)
                        .toList(),
                domain.getTotalElements(),
                domain.getPageable(),
                "searches");
    }

    @Builder
    public record SearchResource(
            Long id,
            String queryKey,
            String query,
            String description,
            boolean publish,
            int views,
            String shortUrl,
            Long hostId,
            String imageUrl) {
        public static SearchResource fromDomain(Search search) {
            return SearchResource.builder()
                    .id(search.getId())
                    .queryKey(search.getQueryKey())
                    .query(search.getQuery())
                    .description(search.getDescription())
                    .publish(search.isPublish())
                    .views(search.getViews())
                    .shortUrl(search.getShortUrl())
                    .hostId(search.getHostId())
                    .imageUrl(search.getShortUrl())
                    .build();
        }
    }
}
