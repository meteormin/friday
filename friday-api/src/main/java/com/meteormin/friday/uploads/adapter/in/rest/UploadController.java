package com.meteormin.friday.uploads.adapter.in.rest;

import com.meteormin.friday.api.uploads.UploadApi;
import com.meteormin.friday.api.uploads.resource.UploadFileResources;
import com.meteormin.friday.api.uploads.resource.UploadFileResources.UploadFileResource;
import com.meteormin.friday.common.hexagon.BaseController;
import com.meteormin.friday.common.hexagon.annotation.RestAdapter;
import com.meteormin.friday.common.request.UploadFile;
import com.meteormin.friday.infrastructure.security.PrincipalUserInfo;
import com.meteormin.friday.infrastructure.security.annotation.AuthUser;
import com.meteormin.friday.uploads.application.port.in.UploadUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

@RequiredArgsConstructor
@RestAdapter(path = UploadApi.PATH)
public class UploadController extends BaseController implements UploadApi {
    private final UploadUsecase usecase;

    @Override
    @PostMapping(path = "",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UploadFileResource> upload(
            @RequestPart(name = "file") UploadFile uploadFile,
            @AuthUser PrincipalUserInfo userInfo) {

        var domain = usecase.upload(userInfo.getId(), uploadFile);
        return ResponseEntity.created(createUri("/", domain.getId()))
                .body(UploadFileResource.fromDomain(domain));
    }

    @Override
    public ResponseEntity<UploadFileResource> findById(Long id) {
        var domain = usecase.findById(id);
        return ResponseEntity.ok(UploadFileResource.fromDomain(domain));
    }

    @Override
    public ResponseEntity<UploadFileResources> findAll(
            @AuthUser PrincipalUserInfo userInfo) {
        var domain = usecase.findAll(userInfo.getId());
        return ResponseEntity.ok(UploadFileResources.fromDomains(domain));
    }
}
