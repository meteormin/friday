package com.meteormin.friday.infrastructure.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private String path;
    private String originName;
    private Long size;
    private String convName;
    private String mimeType;
}
