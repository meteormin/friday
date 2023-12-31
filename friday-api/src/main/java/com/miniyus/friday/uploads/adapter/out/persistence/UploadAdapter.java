package com.miniyus.friday.uploads.adapter.out.persistence;

import com.miniyus.friday.common.hexagon.annotation.PersistenceAdapter;
import com.miniyus.friday.common.request.UploadFile;
import com.miniyus.friday.infrastructure.filesystem.FileSystemManager;
import com.miniyus.friday.infrastructure.filesystem.LocalFileSystemAdapter;
import com.miniyus.friday.infrastructure.persistence.entities.FileEntity;
import com.miniyus.friday.infrastructure.persistence.repositories.FileEntityRepository;
import com.miniyus.friday.uploads.application.port.out.UploadPort;
import com.miniyus.friday.uploads.domain.UploadFileDomain;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UploadAdapter implements UploadPort {
    private final FileSystemManager fileSystemManager;
    private final FileEntityRepository fileEntityRepository;

    @Override
    public UploadFileDomain upload(UploadFile uploadFile) throws IOException {
        var fileAdapter = fileSystemManager.getAdapterWrapper(LocalFileSystemAdapter.class);
        var fileEntity = fileAdapter.save(uploadFile.getFilename(), uploadFile);
        return toDomain(fileEntity);
    }

    @Override
    public Optional<UploadFileDomain> findById(Long id) {
        return fileEntityRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public List<UploadFileDomain> findAll(Long userId) {
        return fileEntityRepository.findByUserId(userId)
            .stream().map(this::toDomain).toList();
    }

    private UploadFileDomain toDomain(FileEntity entity) {
        var fileAdapter = fileSystemManager.getAdapterWrapper(LocalFileSystemAdapter.class);
        return UploadFileDomain.builder()
            .id(entity.getId())
            .path(entity.getPath())
            .originName(entity.getOriginName())
            .convName(entity.getConvName())
            .mimeType(entity.getMimeType())
            .size(entity.getSize())
            .url(fileAdapter.getUrl(entity))
            .build();
    }

}
