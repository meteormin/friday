package com.meteormin.friday.infrastructure.filesystem;

import com.meteormin.friday.infrastructure.filesystem.exception.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileSystemManager {
    private static final String BASE_PATH = "data";
    private final List<FileSystemAdapter> adapters;
    private final Map<String, FileSystemAdapterWrapper> fileSystemAdapterWrappers = new HashMap<>();

    private FileSystemAdapter getAdapter(Class<? extends FileSystemAdapter> clazz) {
        return adapters.stream()
                .filter(a -> clazz.isAssignableFrom(a.getClass()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("not found adapter"));
    }

    public FileSystemAdapterWrapper getAdapterWrapper(Class<? extends FileSystemAdapter> clazz) {
        if (fileSystemAdapterWrappers.containsKey(clazz.getName())) {
            return fileSystemAdapterWrappers.get(clazz.getName());
        } else {
            var wrapper = new FileSystemAdapterWrapper(getAdapter(clazz));
            fileSystemAdapterWrappers.put(clazz.getName(), wrapper);
            return wrapper;
        }
    }

    @RequiredArgsConstructor
    public static class FileSystemAdapterWrapper implements FileSystemAdapter {
        private final FileSystemAdapter adapter;

        public FileInfo save(String path, MultipartFile multipartFile) throws IOException {
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new FileNotFoundException("File is empty", null);
            }

            var uuid = UUID.randomUUID();

            String originName = uuid.toString();
            if (multipartFile.getOriginalFilename() != null) {
                originName = multipartFile.getOriginalFilename();
            }

            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            if (multipartFile.getContentType() != null) {
                mimeType = multipartFile.getContentType();
            }

            var fileInfo = FileInfo.builder()
                    .path(makePath(path, uuid.toString()))
                    .originName(originName)
                    .size(multipartFile.getSize())
                    .convName(uuid.toString())
                    .mimeType(mimeType)
                    .build();


            adapter.put(fileInfo.getPath(), multipartFile);

            return fileInfo;
        }

        @Override
        public boolean put(String path, MultipartFile multipartFile) throws IOException {
            return adapter.put(path, multipartFile);
        }

        @Override
        public URL getUrl(String path) {
            return adapter.getUrl(path);
        }

        @Override
        public ResponseEntity<UrlResource> download(String path) {
            return adapter.download(path);
        }

        @Override
        public boolean delete(String path) {
            return adapter.delete(path);
        }

        @Override
        public boolean exists(String path) {
            return adapter.exists(path);
        }


        @Override
        public File getFile(String path) throws IOException {
            return adapter.getFile(path);
        }

        private String makePath(String... path) {
            var p = Paths.get(FileSystemManager.BASE_PATH, path).toString();
            return p.replace("\\", "/")
                    .replace("//", "/");
        }
    }
}
