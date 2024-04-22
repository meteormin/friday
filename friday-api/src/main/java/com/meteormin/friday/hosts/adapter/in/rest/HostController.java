package com.meteormin.friday.hosts.adapter.in.rest;

import com.meteormin.friday.api.hosts.HostApi;
import com.meteormin.friday.api.hosts.request.CreateHostRequest;
import com.meteormin.friday.api.hosts.request.RetrieveHostRequest;
import com.meteormin.friday.api.hosts.request.UpdateHostRequest;
import com.meteormin.friday.api.hosts.resource.HostResources;
import com.meteormin.friday.hexagon.BaseController;
import com.meteormin.friday.hexagon.annotation.RestAdapter;
import com.meteormin.friday.common.request.annotation.QueryParam;
import com.meteormin.friday.hosts.application.port.in.query.RetrieveHostQuery;
import com.meteormin.friday.hosts.application.port.in.usecase.HostUsecase;
import com.meteormin.friday.hosts.domain.HostIds;
import com.meteormin.friday.infrastructure.security.PrincipalUserInfo;
import com.meteormin.friday.infrastructure.security.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.meteormin.friday.api.hosts.resource.HostResources.HostResource;

@RestAdapter(path = HostApi.PATH)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('user', 'admin')")
public class HostController extends BaseController implements HostApi {
    private final HostUsecase hostUsecase;
    private final RetrieveHostQuery retrieveHostQuery;

    @Override
    @PostMapping("")

    public ResponseEntity<HostResource> createHost(
            @RequestBody @Valid CreateHostRequest request,
            @AuthUser PrincipalUserInfo userInfo) {
        var host = hostUsecase.createHost(request.toDomain(userInfo.getId()));

        return ResponseEntity.created(createUri("/{id}", host.getId()))
                .body(HostResource.fromDomain(host));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<HostResource> updateHost(
            @PathVariable Long id,
            @RequestBody @Valid UpdateHostRequest request,
            @AuthUser PrincipalUserInfo userInfo) {
        var host = hostUsecase.patchHost(request.toDomain(id, userInfo.getId()));
        return ResponseEntity.ok(HostResource.fromDomain(host));
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteHost(
            @PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        hostUsecase.deleteHostById(
                HostIds.builder()
                        .id(id)
                        .userId(userInfo.getId())
                        .build());
    }

    @Override
    @GetMapping("")
    public ResponseEntity<HostResources> retrieveHosts(
            @QueryParam @Valid RetrieveHostRequest req,
            @PageableDefault(
                    page = 1,
                    sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthUser PrincipalUserInfo userInfo) {
        var domains = retrieveHostQuery.retrieveHosts(
                req.toDomain(userInfo.getId(), pageable));

        return ResponseEntity.ok(
                new HostResources(domains));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<HostResource> retrieveHost(
            @PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        var host = retrieveHostQuery.retrieveHostById(
                HostIds.builder()
                        .id(id)
                        .userId(userInfo.getId())
                        .build());

        return ResponseEntity.ok(HostResource.fromDomain(host));
    }
}
