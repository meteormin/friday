package com.meteormin.friday.infrastructure.filesystem;

import com.meteormin.friday.common.util.UploadFileUtil;
import com.meteormin.friday.infrastructure.filesystem.exception.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class LocalFileSystemAdapter implements FileSystemAdapter {
    @Override
    public boolean put(String filePath, MultipartFile multipartFile) throws IOException {
        Path path = Paths.get(filePath);

        Files.createDirectories(path.getParent());

        if (Files.exists(path)) {
            File file = new File(filePath);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                int bytes = FileCopyUtils.copy(multipartFile.getInputStream(), outputStream);
                return bytes != 0;
            }
        }

        Files.createFile(path);
        return true;
    }

    @Override
    public URL getUrl(String path) {
        try {
            return getFile(path).toURI().toURL();
        } catch (IOException e) {
            throw newFileNotFoundException(e);
        }
    }

    @Override
    public ResponseEntity<UrlResource> download(String path) {
        UrlResource resource;
        String filename;
        String contentDisposition;
        MimeType mimeType;
        long contentLength;

        try {
            resource = new UrlResource("file:" + path);
            contentLength = resource.contentLength();
            filename = resource.getFilename();
            contentDisposition = "attachment; filename=\"" + filename + "\"";
            mimeType = UploadFileUtil.getMimeType(path);
        } catch (IOException e) {
            throw newFileNotFoundException(e);
        }

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(mimeType.toString());
        } catch (InvalidMediaTypeException e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(contentLength)
                .contentType(mediaType)
                .body(resource);
    }

    @Override
    public boolean delete(String path) {
        try {
            return Files.deleteIfExists(Path.of(getFile(path).getPath()));
        } catch (IOException e) {
            throw newFileNotFoundException(e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    @Override
    public File getFile(String filePath) throws IOException {
        if (exists(filePath)) {
            return new File(filePath);
        }
        return null;
    }

    private FileNotFoundException newFileNotFoundException(Throwable throwable) {
        return new FileNotFoundException("File not found", throwable);
    }
}
