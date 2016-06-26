package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputQueries implements Iterable<TrecQuery> {

    List<TrecQuery> trecQueries;

    public InputQueries(String queryFileName) throws IOException {
        this.trecQueries = new ArrayList<TrecQuery>();
        Path queryPath = Paths.get(queryFileName);
        BufferedReader queryFileReader = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8);
        readQueryFile(queryFileReader);
    }

    @Override
    public Iterator<TrecQuery> iterator() {
        return trecQueries.iterator();
    }

    private void readQueryFile(BufferedReader queryFileReader) throws IOException {
        String currentLine;
        while ((currentLine = queryFileReader.readLine()) != null) {
            currentLine = currentLine.trim();
            if (currentLine == null || currentLine.isEmpty()) {
                break;
            }
            String topicID = currentLine.split("=")[1];
            String inputQuery = queryFileReader.readLine();
            TrecQuery trecQuery = new TrecQuery(topicID, inputQuery);
            this.trecQueries.add(trecQuery);
        }
    }
}
