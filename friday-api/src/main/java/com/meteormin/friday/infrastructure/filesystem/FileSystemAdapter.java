package com.meteormin.friday.infrastructure.filesystem;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * File System Adapter.
 *
 * @author seongminyoo
 * @since 2023/12/12
 */
public interface FileSystemAdapter {
    /**
     * Saves a file at the specified path.
     *
     * @param path the path where the file will be saved
     * @param multipartFile the file to be saved
     * @return true if the file was successfully saved, false otherwise
     * @throws IOException if there is an error while saving the file
     */
    boolean put(String path, MultipartFile multipartFile) throws IOException;

    /**
     * Retrieves the URL for the given path.
     *
     * @param path the path to retrieve the URL for
     * @return the URL for the given path
     */
    URL getUrl(String path);

    /**
     * Downloads a file from the given path.
     *
     * @param path the path of the file to be downloaded
     * @return the ResponseEntity containing the downloaded file as a UrlResource object
     */
    ResponseEntity<UrlResource> download(String path);

    /**
     * Deletes the file or directory specified by the given path.
     *
     * @param path the path of the file or directory to be deleted
     * @return true if the file or directory is successfully deleted, false otherwise
     */
    boolean delete(String path);

    /**
     * Checks whether a file or directory exists at the specified path.
     *
     * @param path the path of the file or directory to check
     * @return true if the file or directory exists, false otherwise
     */
    boolean exists(String path);

    /**
     * Retrieves a File object based on the provided path.
     *
     * @param path The path to the file.
     * @return The File object representing the file at the specified path.
     * @throws IOException If an I/O error occurs while retrieving the file.
     */
    File getFile(String path) throws IOException;
}
