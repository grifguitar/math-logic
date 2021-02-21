package TaskB.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FastScanner {
    private final BufferedReader reader;

    public FastScanner(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public String nextLine() {
        try {
            String line = reader.readLine();
            return (line != null) ? line.replaceAll("\\s", "") : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}