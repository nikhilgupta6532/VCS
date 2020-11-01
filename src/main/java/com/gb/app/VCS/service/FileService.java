package com.gb.app.VCS.service;

import com.gb.app.VCS.models.FileHistory;
import com.gb.app.VCS.models.FileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileInfo fileUpload(MultipartFile file, String fileId);

    FileInfo getFile(String id,String ver);

    FileInfo updateFile(String id,String ver,MultipartFile file);

    FileHistory getHistory(String id);

    String revertFile(String revVersion,String id);
}
