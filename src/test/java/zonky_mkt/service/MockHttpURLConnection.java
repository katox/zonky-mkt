package zonky_mkt.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockHttpURLConnection extends HttpURLConnection {

    protected MockHttpURLConnection(URL url) {
        super(url);
    }

    @Override
    public int getResponseCode() throws IOException {
        return 200;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Load file path-of-url.json from the test resources of the project
        if (url.getPath() == null || url.getPath().isBlank()) {
            throw new IOException("Requested URL path can't be empty!");
        }
        String filename = url.getPath() + ".json";
        InputStream mockInput = this.getClass().getResourceAsStream(filename);
        if (mockInput == null) {
            throw new IOException("Mock response '" + filename + "' not found on the classpath.");
        }
        return mockInput;
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

}