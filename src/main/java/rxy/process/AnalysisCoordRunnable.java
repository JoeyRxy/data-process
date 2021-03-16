package rxy.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class AnalysisCoordRunnable implements Runnable {

    private File outputDir;
    private int combFactor;
    private int limit;
    private Tuple<Integer, Integer> coord;
    private List<File> files;
    private int[] pciList;
    private Map<Integer, List<List<Double>>> pciDistMap;
    private List<Double>[] memo;
    private Set<List<Double>> xSet;

    public AnalysisCoordRunnable(File outputDir, int combFactor, int limit, Tuple<Integer, Integer> coord,
            List<File> files) {
        this.outputDir = outputDir;
        this.combFactor = combFactor;
        this.limit = limit;
        this.coord = coord;
        this.files = files;
        this.pciList = new int[Processor.pciSet.size()];
        int _i = -1;
        for (Integer pci : Processor.pciSet)
            pciList[++_i] = pci;
        Arrays.sort(pciList);
    }

    @Override
    public void run() {
        System.out.println("================ " + coord + " : Start ==================");
        try {
            analyzeFreq(readAll(files));
            // readAll(files);
        } catch (IOException e) {
            System.err.println(coord);
            e.printStackTrace();
        }
        System.out.println("================ " + coord + " : Done , size: " + xSet.size() + " ===================");
    }

    public static List<Tuple<Integer, Integer>> read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        String line;
        LinkedList<Tuple<Integer, Integer>> list = new LinkedList<>();
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(",");
            if (split.length < 14)
                break;
            try {
                int pci = Integer.parseInt(split[6]);
                int dbm = Integer.parseInt(split[13]);
                // Integer count = Processor.pciDbmCountMap.get(pci);
                // if (count == null)
                //     Processor.pciDbmCountMap.put(pci, 1);
                // else
                //     Processor.pciDbmCountMap.put(pci, count + 1);
                list.add(new Tuple<>(pci, dbm));
            } catch (NumberFormatException e) {
                System.err.println("can't parse integer: " + split[6] + ", " + split[13]);
            }
        }
        reader.close();
        return list;
    }

    public static List<Tuple<Integer, Integer>> readAll(Collection<File> files) throws IOException {
        List<Tuple<Integer, Integer>> ret = new LinkedList<>();
        for (File file : files)
            ret.addAll(read(file));
        return ret;
    }

    public void analyzeFreq(List<Tuple<Integer, Integer>> pciDbmList) throws IOException {
        if (pciDbmList == null || pciDbmList.size() == 0)
            return;
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (Tuple<Integer, Integer> pci_dbm : pciDbmList) {
            if (Processor.pciSet != null && !Processor.pciSet.contains(pci_dbm.t1))
                continue;
            List<Integer> dbmList = map.get(pci_dbm.t1);
            if (dbmList == null) {
                dbmList = new ArrayList<>();
                map.put(pci_dbm.t1, dbmList);
            }
            dbmList.add(pci_dbm.t2);
        }

        Map<Integer, FreqAnalysisRunnable> taskMap = new HashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(Processor.N);
        for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
            FreqAnalysisRunnable task = new FreqAnalysisRunnable(entry.getValue(), combFactor, limit);
            taskMap.put(entry.getKey(), task);
            pool.execute(task);
        }
        pool.shutdown();
        pciDistMap = new HashMap<>();
        for (Entry<Integer, FreqAnalysisRunnable> entry : taskMap.entrySet()) 
            pciDistMap.put(entry.getKey(), entry.getValue().get());
        if (Processor.pciSet != null)
            for (int pci : Processor.pciSet) {
                if (!pciDistMap.containsKey(pci)) {
                    Double[] _dist = new Double[98];
                    for (int i = 0; i < 98; i++) {
                        _dist[i] = 0.;
                    }
                    _dist[1] = 1.;
                    pciDistMap.put(pci, List.of(List.of(_dist)));
                }
            }

        FileOutputStream out = new FileOutputStream(new File(outputDir, "file-" + coord + "-pci-dist-map.json"));
        JSONObject.writeJSONString(out, StandardCharsets.UTF_8, pciDistMap,
                SerializerFeature.WriteNonStringKeyAsString);
        out.close();
        //
        memo = new List[pciList.length];

        // int size = 1;
        // for (List<double[]> freqDist : pciDistMap.values())
        //     size *= freqDist.size();
        done = false;
        xSet = new HashSet<>();
        dfs(0);
        pciDistMap = null;
        memo = null;

        out = new FileOutputStream(new File(outputDir, "file-" + coord + ".json"));
        JSONArray.writeJSONString(out, StandardCharsets.UTF_8, xSet);
        out.close();
    }

    private boolean done = false;
    protected static int totallimit = 10000;

    private void dfs(int depth) {
        if (depth == pciList.length) {
            int n = memo.length, m = memo[0].size();
            if (m != 98) {
                System.err.println("Something Goes Wrong!");
                return;
            }
            int total = 0;
            ArrayList<Double> t = new ArrayList<>(m * n);
            for (int i = 0; i < n; i++) {
                List<Double> memo_i = memo[i];
                total += memo_i.get(0);
                t.addAll(memo_i);
            }
            if (total == 0) 
                System.err.println("格子" + coord + "下没有数据！");
            // else if (total > 1000) {
            //     done = true;
            //     return;
            // }
            else 
                for (int i = 0; i < n; ++i)
                    t.set(i * m, t.get(i * m) / total);
            
            xSet.add(t);
            done = (xSet.size() > totallimit);
            return;
        }
        for (List<Double> freqDist : pciDistMap.get(pciList[depth])) {
            if (done) break;
            memo[depth] = freqDist;
            dfs(depth + 1);
        }
    }

}
