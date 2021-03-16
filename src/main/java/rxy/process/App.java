package rxy.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {
        Processor.N = Runtime.getRuntime().availableProcessors();
        AnalysisCoordRunnable.totallimit = 10000;
        // Processor.N = 1;
        Processor.pciSet = new HashSet<>(List.of(425, 137, 87, 424, 400, 309, 215, 915));
        ArrayList<File> roots = new ArrayList<>();
        File _root = new File("/home/mix21n1/code/data2/");
        // File _root = new File("C:/Users/Rxy/Desktop/project/ai/py/data/");
        // File _root = new File("C:/Users/Rxy/Downloads/another");
        // File _root = new File("C:/Users/Rxy/Downloads/another2");
        for (File root : _root.listFiles())
            if (root.isDirectory())
                roots.add(root);
        File outputDir = new File("totaldata7/");
        if (!outputDir.exists())
            outputDir.mkdirs();
        Processor.process(roots, outputDir, 5, 5000);
        // TreeMap<Integer, List<Integer>> map = new TreeMap<>();
        // for (Entry<Integer, Integer> entry : Processor.pciDbmCountMap.entrySet()) {
        //     int pci = entry.getKey();
        //     int count = entry.getValue();
        //     List<Integer> list = map.get(count);
        //     if (list == null) {
        //         list = new LinkedList<>();
        //         map.put(count, list);
        //     }
        //     list.add(pci);
        // }
        // System.out.println(map);
        File datafinalDir = new File("finaldata3");
        if (!datafinalDir.exists()) datafinalDir.mkdirs();
        Processor.processTotal(outputDir, datafinalDir);
    }

}