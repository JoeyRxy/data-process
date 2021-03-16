package rxy.process;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FreqAnalysisRunnable implements Runnable {
    private Object lock = new Object();
    private boolean done = false;

    private List<Integer> dbmList;
    private int combFactor;
    private int limit;
    private List<List<Double>> ret;

    public FreqAnalysisRunnable(List<Integer> dbmList, int combFactor, int limit) {
        this.dbmList = dbmList;
        this.combFactor = combFactor;
        this.limit = limit;
    }

    public static Double[] helper(List<Integer> dbmList) {
        int l = dbmList.size();
        Double[] freq = new Double[98];
        freq[0] = Double.valueOf(l);
        for (int i = 1; i < 98; i++) {
            freq[i] = 0.;
        }
        // freq[0] = 0;
        for (Integer dbm : dbmList) {
            if (dbm == null || dbm == 2147483647 || dbm <= -140)
                ++freq[1];
            else if (dbm >= -44)
                ++freq[97];
            else
                ++freq[141 + dbm];
        }
        for (int i = 1; i < 98; ++i)
            freq[i] /= l;
        return freq;
    }


    @Override
    public void run() {
        System.out.println("start");
        int l = dbmList.size();
        int diffNum = new HashSet<>(dbmList).size();
        if ((l >> 1) <= combFactor || diffNum <= 3) {
            ret = List.of(List.of(helper(dbmList)));
        } else {
            ret =  new LinkedList<List<Double>> ();
            for (int m = l - combFactor; m <= l; ++m) {
                CombIter<Integer> iter = new CombIter<>(dbmList, m);
                Set<List<Double>> set = new HashSet<>();
                while(iter.hasNext() && set.size() < limit) {
                    set.add(List.of(helper(iter.next())));
                }
                ret.addAll(set);
            }
        }
        System.out.println("Done, result set size: " + ret.size());
        // done 参数可以保证当run()已经完成时，get()方法不会调用wait()方法
        done = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    public List<List<Double>> get() {
        synchronized (lock) {
            try {
                while (!done)
                    lock.wait();
            } catch (InterruptedException e) {
            }
        }
        return ret;
    }

}
