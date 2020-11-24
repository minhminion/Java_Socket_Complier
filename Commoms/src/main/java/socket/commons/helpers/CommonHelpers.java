package socket.commons.helpers;

import socket.commons.enums.Language;

public class CommonHelpers {
    public static String getFileExtension(String filePath) {
        if(filePath.lastIndexOf(".") != -1 && filePath.lastIndexOf(".") != 0)
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        else return "";
    }

    public static String getFileNameFromPath(String filePath) {
        if(filePath.lastIndexOf("/") != -1 && filePath.lastIndexOf("/") != 0)
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        else return "";
    }

    public static Language getLanguageFromFilePath(String filePath)
    {
        String extension = getFileExtension(filePath);
        Language fileLanguage = null;

        switch (extension)
        {
            case "java": {
                fileLanguage = Language.JAVA;
                break;
            }

            case "cs": {
                fileLanguage = Language.CSHARP;
                break;
            }

            case "cpp":
            case "c": {
                fileLanguage = Language.CPP;
                break;
            }

            case "py": {
                fileLanguage = Language.PYTHON;
                break;
            }

            default: {
                break;
            }
        }

        return fileLanguage;
    }

}

