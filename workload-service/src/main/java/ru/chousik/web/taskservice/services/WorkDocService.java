package ru.chousik.web.taskservice.services;

import java.io.InputStream;
import java.net.URL;

public interface WorkDocService {
    URL uploadWork(String key,
    InputStream data,
    long length,
    String contType);
}
