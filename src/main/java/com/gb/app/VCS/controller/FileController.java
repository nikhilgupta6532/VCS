package com.gb.app.VCS.controller;

import com.gb.app.VCS.exception.FileNotFoundException;
import com.gb.app.VCS.exception.FileStorageException;
import com.gb.app.VCS.models.FileHistory;
import com.gb.app.VCS.models.FileInfo;
import com.gb.app.VCS.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@CrossOrigin("*")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/vcs/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file) {
        LOGGER.info("Method Invocation : {}", "uploadFile");
        String fileId = UUID.randomUUID().toString();
        try {
            FileInfo response = fileService.fileUpload(file, fileId);
            LOGGER.debug("File uploaded successfully with id : {} and ver : {}", response.get_id(), response.get_ver());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (FileStorageException ex) {
            LOGGER.error("Exception Occurred while uploading file : {}", ex.getMsg());
            throw new FileStorageException(ex.getMsg());
        }
    }

    @GetMapping("/vcs/getFile")
    @ResponseBody
    public ResponseEntity<?> getFile(@RequestParam String id, @RequestParam String ver) {
        LOGGER.info("Method Invocation : {}", "getFile");
        try {
            FileInfo response = fileService.getFile(id, ver);
            return generateResponse(HttpStatus.OK, response);
        } catch (FileNotFoundException ex) {
            LOGGER.error("Exception Occurred while fetching file : {}", ex.getMsg());
            throw new FileNotFoundException(ex.getMsg());
        }
    }

    @PostMapping("/vcs/updateFile")
    @ResponseBody
    public ResponseEntity<?> updateFile(@RequestParam String id, @RequestParam String ver, @RequestParam MultipartFile file) {
        LOGGER.info("Method Invocation : {}", "updateFile");
        try {
            FileInfo response = fileService.updateFile(id, ver, file);
            LOGGER.debug("File updated successfully with id : {} and ver : {}", response.get_id(), response.get_ver());
            return generateResponse(HttpStatus.CREATED, response);
        } catch (FileNotFoundException | FileStorageException ex) {
            if (ex instanceof FileNotFoundException)
                throw new FileNotFoundException(((FileNotFoundException) ex).getMsg());
            else
                throw new FileStorageException(((FileStorageException) ex).getMsg());
        }
    }

    @GetMapping("/vcs/history/{id}/versions")
    @ResponseBody
    public ResponseEntity<?> getHistoryOfFile(@PathVariable String id) {
        LOGGER.info("Method Invocation : {}", "getHistoryOfFile");
        FileHistory response = fileService.getHistory(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/vcs/revert/{id}/versions")
    @ResponseBody
    public String revertFile(@RequestParam String revVersion, @PathVariable String id) {
        LOGGER.info("Method Invocation : {}", "revertFile");
        try {
            return fileService.revertFile(revVersion, id);
        } catch (FileStorageException ex) {
            throw new FileStorageException(ex.getMsg());
        }
    }


    private ResponseEntity<?> generateResponse(HttpStatus httpStatus, FileInfo response) {
        LOGGER.info("Method Invocation : {}", "generateResponse");
        if (ObjectUtils.isEmpty(response.getError())) {
            return ResponseEntity.status(httpStatus).body(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response.getError());
        }
    }
}
