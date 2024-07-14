package com.diplom.faces_recognition.utils.log;

public interface ILog {
    void warn(String message);
    void error(String message);
    void info(String message);

    ILog init(Class<?> className);
}
