package rxy.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class Processor {
    protected static int N = 16;
    public static HashSet<Integer> pciSet = null;
    public static ConcurrentHashMap<Integer, Integer> pciDbmCountMap = new ConcurrentHashMap<>();

    public static void process(Collection<File> roots, File outputDir, int combFactor, int limit) {
        Map<Tuple<Integer, Integer>, List<File>> map = new HashMap<>();
        for (File root : roots) {
            for (File dir : root.listFiles()) {
                String dirName = dir.getName();
                if (dir.isDirectory()) {
                    Tuple<Integer, Integer> tpl = new Tuple<>(Integer.parseInt(dirName.substring(1, 4)),
                            Integer.parseInt(dirName.substring(5, 8)));
                    List<File> list = map.get(tpl);
                    if (list == null) {
                        list = new LinkedList<>();
                        map.put(tpl, list);
                    }
                    for (File file : dir.listFiles())
                        if (file.getName().endsWith(".csv"))
                            list.add(file);
                }
            }
        }
        //
        // ExecutorService pool = Executors.newFixedThreadPool(1);
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (Entry<Tuple<Integer, Integer>, List<File>> entry : map.entrySet())
            pool.execute(new AnalysisCoordRunnable(outputDir, combFactor, limit, entry.getKey(), entry.getValue()));

        pool.shutdown();
        try {
            while (!pool.awaitTermination(1, TimeUnit.MINUTES))
                System.out.println("Waiting ... ");
        } catch (InterruptedException e) {
            System.err.println("Stop Unexpected!");
            e.printStackTrace();
        }
    }

    public static void processTotal(File inputDir, File outputDir) throws Exception {
        if (!inputDir.isDirectory() || !outputDir.isDirectory())
            return;
        Pattern pat = Pattern.compile("file-(\\(.*\\))\\.json");
        // Map<String, Map<Integer, List<double[]>>> map = new HashMap<>();
        int[] pciList = new int[pciSet.size()];
        int _i = -1;
        for (Integer pci : pciSet)
            pciList[++_i] = pci;
        Arrays.sort(pciList);
        //
        Type jsonType = new TypeReference<LinkedList<ArrayList<Double>>>() {
        }.getType();
        LinkedList<ArrayList<Double>> xData = new LinkedList<>();
        LinkedList<String> yData = new LinkedList<>();
        int __i = 0;
        int totalfileslen = inputDir.list().length >> 1;
        for (File file : inputDir.listFiles()) {
            Matcher matcher = pat.matcher(file.getName());
            if (matcher.matches()) {
                FileInputStream in = new FileInputStream(file);
                System.out.println(String.format("%3d/%3d : %s", (++__i), totalfileslen, file.getName()));
                LinkedList<ArrayList<Double>> xList  = JSONObject.parseObject(in, StandardCharsets.UTF_8, jsonType);
                xData.addAll(xList);
                String[] yList = new String[xList.size()];
                Arrays.fill(yList, matcher.group(1));
                for (String y : yList)
                    yData.add(y);
                in.close();
            }
        }
        // FileOutputStream out = new FileOutputStream(outputFile);
        // JSONObject.writeJSONString(out, StandardCharsets.UTF_8, map,
        // SerializerFeature.WriteNonStringKeyAsString);
        // out.close();
        //
        // shuffle the data order
        if (xData.size() != yData.size()) throw new Exception("Something goes wrong!");
        ArrayList<Double>[] xDataArr = new ArrayList[xData.size()];
        int i = -1;
        for (ArrayList<Double> x : xData) 
            xDataArr[++i] = x;
        xData = null;
        
        // ObjectOutputStream objos = new ObjectOutputStream(new FileOutputStream(new File(outputDir, "x_data.bin")));
        // objos.writeObject(xDataArr);
        // objos.close();
        String[] yDataArr = new String[yData.size()];
        i = -1;
        for (String y: yData) 
            yDataArr[++i] = y;
        yData = null;
        shuffle(xDataArr, yDataArr);
        FileOutputStream os = new FileOutputStream(new File(outputDir, "x_data.json"));
        JSONArray.writeJSONString(os, StandardCharsets.UTF_8, xDataArr);
        os.close();
        os = new FileOutputStream(new File(outputDir, "y_data.json"));
        JSONArray.writeJSONString(os, StandardCharsets.UTF_8, yDataArr);
        os.close();
        // objos = new ObjectOutputStream(new FileOutputStream(new File(outputDir, "y_data.bin")));
        // objos.writeObject(yDataArr);
        // objos.close();
    }

    public static void shuffle(Object[] arr1, Object[] arr2) {
        Random r = new Random(System.currentTimeMillis());
        if (arr1.length != arr2.length) throw new IllegalArgumentException();
        int n = arr1.length;
        for (int i = n - 1; i > 0; --i) {
            int j = r.nextInt(i);
            // swap
            Object t = arr1[i];
            arr1[i] = arr1[j];
            arr1[j] = t;
            t = arr2[i];
            arr2[i] = arr2[j];
            arr2[j] = t;
        }
    }

}
