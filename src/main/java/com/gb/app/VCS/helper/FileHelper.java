package com.gb.app.VCS.helper;

import com.gb.app.VCS.constants.FileConstants;
import com.gb.app.VCS.models.FileHistory;
import com.gb.app.VCS.models.FileInfo;
import com.gb.app.VCS.models.History;
import com.gb.app.VCS.service.impl.FileServiceImpl;
import com.gb.app.VCS.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

    public FileHistory createFileHistory(String id, List<FileInfo> allFileVersions) {
        LOGGER.info("Method Invocation : {}", "createFileHistory");
        FileHistory fileHistory = new FileHistory();
        fileHistory.setId(id);
        List<History> histories = allFileVersions.stream()
                .map(this::generateFileHistory)
                .collect(Collectors.toList());
        fileHistory.setChanges(histories);
        LOGGER.info("Method Termination : {}", "createFileHistory");
        return fileHistory;
    }

    public FileInfo saveNewFile(MultipartFile file, String fileId) throws IOException {
        LOGGER.info("Method Invocation : {}", "saveNewFile");
        String fileVer = UUID.randomUUID().toString();
        String filePath = saveFile(file);
        FileInfo fileInfo = new FileInfo();
        fileInfo.set_id(fileId);
        fileInfo.set_ver(fileVer);
        fileInfo.setPath(filePath);
        fileInfo.setName(file.getOriginalFilename());
        fileInfo.setRevision(1);
        getFileContent(fileInfo);
        LOGGER.debug("File content fetched successfully for id : {} and ver : {}", fileInfo.get_id(), fileInfo.get_ver());
        LOGGER.info("Method Termination : {}", "saveNewFile");
        return fileInfo;
    }

    public FileInfo saveUpdatedFile(MultipartFile file, FileInfo oldFile) throws IOException {
        LOGGER.info("Method Invocation : {}", "saveUpdatedFile");
        String fileVer = UUID.randomUUID().toString();
        String filePath = saveFile(file);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRevision(oldFile.getRevision() + 1);
        fileInfo.set_id(oldFile.get_id());
        fileInfo.set_ver(fileVer);
        fileInfo.setPath(filePath);
        fileInfo.setName(file.getOriginalFilename());
        getFileContent(fileInfo);
        LOGGER.debug("File content fetched successfully for id : {} and ver : {}", fileInfo.get_id(), fileInfo.get_ver());
        LOGGER.info("Method Termination : {}", "saveUpdatedFile");
        return fileInfo;
    }

    public void getFileContent(FileInfo response) {
        LOGGER.info("Method Invocation : {}", "getFileContent");
        try {
            File file = new File(CommonUtils.getProjectPath() + response.getName());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            StringBuilder content = new StringBuilder();
            while (line != null) {
                content.append(line).append(System.lineSeparator());
                line = br.readLine();
            }
            response.setContent(content.toString());
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        LOGGER.info("Method Termination : {}", "getFileContent");
    }

    public String saveFile(MultipartFile file) throws IOException {
        LOGGER.info("Method Invocation : {}", "saveFile");
        byte[] fileBytes = file.getBytes();
        String filePath = CommonUtils
                .formatMessage(CommonUtils.getProjectPath() + FileConstants.UPLOAD_FOLDER
                        , new Object[]{file.getOriginalFilename()});
        Path path = Paths.get(filePath);
        Files.write(path, fileBytes);
        LOGGER.info("Method Termination : {}", "saveFile");
        return filePath;
    }

    public void writeInFile(String content, String path) throws IOException {
        LOGGER.info("Method Invocation : {}", "writeInFile");
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(content);
        fileWriter.close();
        LOGGER.info("Method Termination : {}", "writeInFile");
    }

    public String[] diffHelper(String a, String b, Map<Long, String[]> lookup) {
        return lookup.computeIfAbsent(((long) a.length()) << 32 | b.length(), k -> {
            if (a.isEmpty() || b.isEmpty()) {
                return new String[]{a, b};
            } else if (a.charAt(0) == b.charAt(0)) {
                return diffHelper(a.substring(1), b.substring(1), lookup);
            } else {
                String[] aa = diffHelper(a.substring(1), b, lookup);
                String[] bb = diffHelper(a, b.substring(1), lookup);
                if (aa[0].length() + aa[1].length() < bb[0].length() + bb[1].length()) {
                    return new String[]{a.charAt(0) + aa[0], aa[1]};
                } else {
                    return new String[]{bb[0], b.charAt(0) + bb[1]};
                }
            }
        });
    }

    private History generateFileHistory(FileInfo file) {
        History history = new History();
        history.setVer(file.get_ver());
        history.setContent(file.getContent());
        history.setRevision(file.getRevision());
        history.setDifference(file.getDifference());
        return history;
    }

}
