package ru.sibint.topcoder.enums;

import lombok.Getter;

@Getter
public enum Language {

    JAVA("java", ".java", ""),
    C_SHARP("csharp", ".cs", ".exe"),
    CPP("cpp", ".cpp", ".out");

    private final String value;
    private final String extension;
    private final String runExtension;

    Language(String value, String extension, String runExtension) {
        this.value = value;
        this.extension = extension;
        this.runExtension = runExtension;
    }

    public static Language fromValue(String sValue) {
        for(Language status: Language.values()) {
            if(status.getValue().equals(sValue)) return status;
        }
        return null;
    }

}
