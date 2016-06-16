package logic.search;

/**
 * Created by claterza on 6/16/16.
 */
public interface SearchEngine {
    void searchWithQueryFile(String indexFileName,
                            String queryFileName,
                            String outputFileName) throws Exception;
}
