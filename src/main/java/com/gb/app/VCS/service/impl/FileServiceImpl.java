package com.gb.app.VCS.service.impl;

import com.gb.app.VCS.constants.FileConstants;
import com.gb.app.VCS.exception.FileNotFoundException;
import com.gb.app.VCS.exception.FileStorageException;
import com.gb.app.VCS.helper.FileHelper;
import com.gb.app.VCS.models.Difference;
import com.gb.app.VCS.models.FileHistory;
import com.gb.app.VCS.models.FileInfo;
import com.gb.app.VCS.repository.FileRepository;
import com.gb.app.VCS.service.FileService;
import com.gb.app.VCS.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileHelper fileHelper;

    @Override
    public FileInfo fileUpload(MultipartFile file, String fileId) {
        LOGGER.info("Method Invocation : {}", "fileUpload");
        try {
            FileInfo fileInfo = fileHelper.saveNewFile(file, fileId);
            LOGGER.debug("File saved Successfully for id : {} and ver : {}", fileInfo.get_id(), fileInfo.get_ver());
            return saveFileInfo(fileInfo);
        } catch (IOException ex) {
            throw new FileStorageException("Could Not store file " + file.getOriginalFilename() + ". Please try again");
        }
    }

    @Override
    public FileInfo getFile(String id, String ver) {
        LOGGER.info("Method Invocation : {}", "getFile");
        String docId = CommonUtils.generateDocId(FileConstants.FILE, id, ver);
        LOGGER.debug("docId : {}", docId);
        Optional<FileInfo> file = fileRepository.findById(docId);
        if (file.isPresent()) {
            return file.get();
        }
        LOGGER.error("File Not found for id : {} and ver : {}", id, ver);
        throw new FileNotFoundException("Could Not Find File with id " + id + " and ver " + ver);
    }

    @Override
    public FileInfo updateFile(String id, String ver, MultipartFile file) {
        LOGGER.info("Method Invocation : {}", "updateFile");
        try {
            String docId = CommonUtils.generateDocId(FileConstants.FILE, id, ver);
            LOGGER.debug("docId : {}", docId);
            Optional<FileInfo> oldFile = fileRepository.findById(docId);
            if (oldFile.isPresent()) {
                FileInfo fileInfo = fileHelper.saveUpdatedFile(file, oldFile.get());
                LOGGER.debug("File Updated Successfully for id : {} and ver : {}", fileInfo.get_id(), fileInfo.get_ver());
                String[] differences = fileHelper.diffHelper(oldFile.get().getContent(), fileInfo.getContent(), new HashMap<>());
                Difference difference = new Difference();
                difference.setContentRemoved(differences[0]);
                difference.setContentAdded(differences[1]);
                fileInfo.setDifference(difference);
                return saveFileInfo(fileInfo);
            }
            LOGGER.error("File Not found for id : {} and ver : {}", id, ver);
            throw new FileNotFoundException("Could Not Find File with id " + id + " and ver " + ver);
        } catch (IOException ex) {
            throw new FileStorageException("Could Not store file " + file.getOriginalFilename() + ". Please try again");
        }
    }

    @Override
    public FileHistory getHistory(String id) {
        LOGGER.info("Method Invocation : {}", "getHistory");
        String docId = CommonUtils.generateDocId(FileConstants.FILE, id, FileConstants.PERCENT);
        LOGGER.debug("docId : {}", docId);
        List<FileInfo> allFileVersions = fileRepository.findGetAllVersion(docId);
        return fileHelper.createFileHistory(id, allFileVersions);
    }

    @Override
    public String revertFile(String revVersion, String id) {
        LOGGER.info("Method Invocation : {}", "revertFile");
        String docId = CommonUtils.generateDocId(FileConstants.FILE, id, FileConstants.PERCENT);
        LOGGER.debug("docId : {}", docId);
        List<FileInfo> allFileVersions = fileRepository.findGetAllVersion(docId);
        LOGGER.debug("Total versions of file : {}", allFileVersions.size());

        List<FileInfo> versionsTobeDeleted = allFileVersions.stream()
                .filter(fileInfo -> fileInfo.getRevision() > Integer.parseInt(revVersion))
                .collect(Collectors.toList());

        LOGGER.debug("Total versions to be deleted : {}", versionsTobeDeleted.size());
        fileRepository.deleteAll(versionsTobeDeleted);

        LOGGER.debug("Updating file with revision : {}", revVersion);
        writeCurrentRevisionInFile(allFileVersions, revVersion, id);

        LOGGER.info("Method Termination : {}", "revertFile");
        return "File Updated to Revision " + revVersion;

    }

    private void writeCurrentRevisionInFile(List<FileInfo> allFileVersions, String revVersion, String id) {
        allFileVersions.stream()
                .filter(fileInfo -> fileInfo.getRevision() == Integer.parseInt(revVersion))
                .findAny()
                .ifPresent(updateFile -> {
                    try {
                        fileHelper.writeInFile(updateFile.getContent(), CommonUtils.getProjectPath() + updateFile.getName());
                    } catch (IOException e) {
                        throw new FileStorageException("Exception while writing in file for id " + id);
                    }
                });
    }

    private FileInfo saveFileInfo(FileInfo fileInfo) {
        String docId = CommonUtils.generateDocId(FileConstants.FILE, fileInfo.get_id(), fileInfo.get_ver());
        fileInfo.setId(docId);
        return fileRepository.save(fileInfo);
    }
}
