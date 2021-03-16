package rxy.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class AppTest {

    // @Test
    // public void testJsonArrayOrder() {
    //     Random r = new Random(System.currentTimeMillis());
    //     for (int i = 0; i < 1000000; i++) {
    //         int[] randList = new int[r.nextInt(20) + 10];
    //         for (int j = 0; j < randList.length; j++) {
    //             randList[j] = (int) (100 * r.nextGaussian());
    //         }
    //         String arrStr = JSONArray.toJSONString(randList);
    //         JSONArray arrRecov = JSONArray.parseArray(arrStr);
    //         int len = arrRecov.size();
    //         if (len != randList.length) 
    //             System.err.println("Failed");
    //         for (int j = 0; j < len; j++) 
    //             if (arrRecov.getIntValue(j) != randList[j]) 
    //                 System.err.println("Failed");
    //     }
    //     System.out.println("ok");
    // }

    // private static void f(int[] a) {
    //     a = new int[10];
    //     for (int i = 0; i < a.length; i++) {
    //         a[i] = i;
    //     }
    // }
    
    // @Test
    // public void test() {
    //     int[] a = new int[1];
    //     f(a);
    //     System.out.println(Arrays.toString(a));
    // }

    // @Test
    // public void testShuffle() {
    //     Integer[] arr = new Integer[10];
    //     for (int i = 0; i < arr.length; i++) {
    //         arr[i] = i;
    //     }
    //     Processor.shuffle(arr);
    //     System.out.println(Arrays.toString(arr));
    // }

    @Test
    public void del() {
        File root = new File("C:/Users/Rxy/Desktop/project/ai/py/data/上海闵行区莘东路营业厅1层_P40");
        for (File dir : root.listFiles()) {
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (file.getName().endsWith(".png")) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Test
    public void testComb() {
        // List<Character> list = List.of('a', 'b', 'c', 'd', 'e');
        // CombIter<Character> iter = new CombIter<>(list, 3);
        List<Character> list = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
        CombIter<Character> iter = new CombIter<>(list, 5);
        int i = 0;
        while (iter.hasNext())
            System.out.println((++i) + ": " + iter.next());
    }

    @Test
    public void testComb2() {
        int n = 100;
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        CombIter<Integer> iter2 = new CombIter<>(list, 95);
        int i = 0;
        long start = System.currentTimeMillis();
        while (iter2.hasNext()) {
            iter2.next();
            ++i;
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("%d: %d ms", i, (end - start)));
        //
        CombIter<Integer> iter3 = new CombIter<>(list, 5);
        i = 0;
        start = System.currentTimeMillis();
        while (iter3.hasNext()) {
            iter3.next();
            ++i;
        }
        end = System.currentTimeMillis();
        System.out.println(String.format("%d: %d ms", i, (end - start)));
        //
        // CombIter2<Integer> iter = new CombIter2<>(list, 5);
        // i = 0;
        // start = System.currentTimeMillis();
        // while (iter.hasNext()) {
        //     iter.next();
        //     ++i;
        // }
        // end = System.currentTimeMillis();
        // System.out.println(String.format("%d: %d ms", i, (end - start)));
    }

    @Test
    public void testListHash() {
        Double[] a = {1.,2.,3.};
        Double[] b = {1.,2.,3.};
        Double[] c = {1.,2.,3.000000001};
        Set<List<Double>> set = new HashSet<>();
        set.add(List.of(a));
        set.add(List.of(b));
        set.add(List.of(c));
        System.out.println(set.size());
        //
    }
       
}
