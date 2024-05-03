package com.meteormin.friday.uploads.application.service;

import com.meteormin.friday.common.error.RestErrorCode;
import com.meteormin.friday.common.error.RestErrorException;
import com.meteormin.friday.common.hexagon.annotation.Usecase;
import com.meteormin.friday.common.request.UploadFile;
import com.meteormin.friday.uploads.application.exception.NotFoundUploadFileException;
import com.meteormin.friday.uploads.application.port.in.UploadUsecase;
import com.meteormin.friday.uploads.application.port.out.UploadPort;
import com.meteormin.friday.uploads.domain.UploadFileDomain;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@Usecase
@RequiredArgsConstructor
public class UploadService implements UploadUsecase {
    private final UploadPort port;

    @Override
    public UploadFileDomain upload(Long userId, UploadFile uploadFile) {
        try {
            return port.upload(userId, uploadFile);
        } catch (IOException e) {
            throw new RestErrorException(RestErrorCode.UPLOAD_ERROR, e);
        }
    }

    @Override
    public UploadFileDomain findById(Long id) {
        return port.findById(id)
                .orElseThrow(NotFoundUploadFileException::new);
    }

    @Override
    public List<UploadFileDomain> findAll(Long userId) {
        return port.findAll(userId);
    }
}
