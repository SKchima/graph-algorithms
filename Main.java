import graphs;
import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File(args[0]);
        UndirectedGraph graph = undirectedGraphBuilder();
    }
}
