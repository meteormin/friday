package com.meteormin.friday.uploads.application.port.out;

import com.meteormin.friday.common.request.UploadFile;
import com.meteormin.friday.uploads.domain.UploadFileDomain;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UploadPort {
    UploadFileDomain upload(Long userId, UploadFile uploadFile) throws IOException;

    Optional<UploadFileDomain> findById(Long id);

    List<UploadFileDomain> findAll(Long userId);
}
