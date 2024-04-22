package com.meteormin.friday.uploads.application.port.in;

import com.meteormin.friday.common.request.UploadFile;
import com.meteormin.friday.uploads.domain.UploadFileDomain;

import java.util.List;

public interface UploadUsecase {
    UploadFileDomain upload(Long userId, UploadFile uploadFileDomain);

    UploadFileDomain findById(Long id);

    List<UploadFileDomain> findAll(Long userId);

}
