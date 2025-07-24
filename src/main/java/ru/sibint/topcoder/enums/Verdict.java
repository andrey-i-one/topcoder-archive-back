package ru.sibint.topcoder.enums;

public enum Verdict {

    ACCEPTED("Accepted"),
    WRONG_ANSWER("Wrong answer"),
    MEMORY_LIMIT_EXCEEDED("Memory limit exceeded"),
    TIME_LIMIT_EXCEEDED("Time limit exceeded"),
    COMPILATION_ERROR("Compilation error");

    private final String value;

    Verdict(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Verdict fromValue(String sValue) {
        for(Verdict status: Verdict.values()) {
            if(status.value().equals(sValue)) return status;
        }
        return null;
    }

}
