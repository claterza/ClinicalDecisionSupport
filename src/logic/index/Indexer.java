package logic.index;

import java.io.IOException;

/**
 * Created by claterza on 6/16/16.
 */
public interface Indexer {
    void indexDirectory (String fileName) throws IOException;
}
