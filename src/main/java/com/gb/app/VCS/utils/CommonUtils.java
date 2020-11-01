package com.gb.app.VCS.utils;

import com.gb.app.VCS.constants.FileConstants;
import org.springframework.util.ObjectUtils;

import java.text.MessageFormat;

public interface CommonUtils {

    static String formatMessage(String pattern, Object[] obj) {
        MessageFormat format = new MessageFormat(pattern);
        return format.format(obj);
    }

    static String getProjectPath() {
        String dirPath = System.getProperty("user.dir");
        dirPath = dirPath + "\\uploadFiles\\";
        return dirPath;
    }

    static String generateDocId(String... args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            builder = builder.append(args[i]);
            if (i < (args.length - 1)) {
                builder = builder.append(FileConstants.DOUBLE_COLON);
            }
        }
        return ObjectUtils.getDisplayString(builder);
    }
}
