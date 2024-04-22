package com.meteormin.friday.hosts.adapter.in.rest;

import com.meteormin.friday.api.searches.SearchApi;
import com.meteormin.friday.api.searches.request.CreateSearchRequest;
import com.meteormin.friday.api.searches.request.RetrieveSearchRequest;
import com.meteormin.friday.api.searches.request.UpdateSearchRequest;
import com.meteormin.friday.api.searches.resource.SearchResources;
import com.meteormin.friday.api.searches.resource.SearchResources.SearchResource;
import com.meteormin.friday.hexagon.BaseController;
import com.meteormin.friday.hexagon.annotation.RestAdapter;
import com.meteormin.friday.common.request.annotation.QueryParam;
import com.meteormin.friday.hosts.application.port.in.query.RetrieveSearchQuery;
import com.meteormin.friday.hosts.application.port.in.usecase.SearchUsecase;
import com.meteormin.friday.hosts.domain.searches.SearchIds;
import com.meteormin.friday.infrastructure.security.PrincipalUserInfo;
import com.meteormin.friday.infrastructure.security.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestAdapter(path = SearchApi.PATH)
@PreAuthorize("hasAnyAuthority('user', 'admin')")
public class SearchController extends BaseController implements SearchApi {
    private final SearchUsecase searchUsecase;
    private final RetrieveSearchQuery searchQuery;

    @Override
    @PostMapping("")
    public ResponseEntity<SearchResource> createHostSearch(
            @PathVariable Long hostId,
            @RequestBody @Valid CreateSearchRequest request,
            @AuthUser PrincipalUserInfo userInfo) {
        var domain = searchUsecase.createSearch(
                request.toDomain(hostId));

        return ResponseEntity.created(createUri("/{searchId}", domain.getId()))
                .body(SearchResource.fromDomain(domain));
    }

    @Override
    @GetMapping("")
    public ResponseEntity<SearchResources> retrieveHostSearches(
            @PathVariable Long hostId,
            @QueryParam RetrieveSearchRequest request,
            @PageableDefault(page = 1,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @AuthUser PrincipalUserInfo userInfo) {
        var domain = searchQuery.retrieveSearches(
                request.toDomain(userInfo.getId(), hostId, pageable));
        return ResponseEntity.ok(new SearchResources(domain));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<SearchResource> retrieveHostSearch(
            @PathVariable Long hostId,
            @PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        var domain = searchQuery.retrieveSearch(
                SearchIds.builder()
                        .hostId(hostId)
                        .id(id)
                        .userId(userInfo.getId())
                        .build());
        return ResponseEntity.ok(SearchResource.fromDomain(domain));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<SearchResource> updateHostSearch(
            @PathVariable Long hostId,
            @PathVariable Long id,
            @RequestBody @Valid UpdateSearchRequest request,
            @AuthUser PrincipalUserInfo userInfo) {
        var domain = searchUsecase.patchSearch(
                request.toDomain(
                        SearchIds.builder()
                                .hostId(hostId)
                                .id(id)
                                .userId(userInfo.getId())
                                .build()));

        return ResponseEntity.ok(SearchResource.fromDomain(domain));
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteHostSearch(
            @PathVariable Long hostId,
            @PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        searchUsecase.deleteSearchById(
                SearchIds.builder()
                        .hostId(hostId)
                        .id(id)
                        .userId(userInfo.getId())
                        .build());
    }
}
