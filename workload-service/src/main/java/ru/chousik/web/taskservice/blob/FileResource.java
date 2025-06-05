package ru.chousik.web.taskservice.blob;

import java.io.IOException;
import java.io.InputStream;

public record FileResource(
        InputStream stream,
        long length,
        String contentType
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
