package rxy.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombIter2<T> implements Iterator<ArrayList<T>> {

    private CombRunner<T> runner;
    private Thread thread;

    public CombIter2(List<T> list, int m) {
        if (list == null || m < 0 || m > list.size()) throw new IllegalArgumentException();
        runner = new CombRunner<>(list, m);
        thread = new Thread(runner, "combination-computation-runner");
        thread.start();
    }

    @Override
    public boolean hasNext() {
        return !runner.done;
    }

    @Override
    public ArrayList<T> next() {
        try {
            return runner.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        runner.done = true;
        thread.interrupt();
    }

}

class CombRunner<T> implements Runnable {

    private List<T> list;
    private final int m;
    private boolean ready, wanted;
    protected boolean done;
    private ArrayList<T> ret;
    private T[] memo;
    private Object lock = new Object();
    private boolean[] vis;
    private int n;

    public CombRunner(List<T> list, int m) {
        this.list = (list instanceof ArrayList) ? list : new ArrayList<>(list);
        this.m = m;
        this.n = list.size();
        memo = (T[]) new Object[m];
        vis = new boolean[n];
    }

    @Override
    public void run() {
        try {
            dfs(0, 0);
        } catch (InterruptedException e) {
        }
        done = true;
    }

    private void dfs(int depth, int from) throws InterruptedException {
        if (depth == m) {
            synchronized (lock) {
                while (!wanted && !done) lock.wait();
            }
            wanted = false;
            ret = new ArrayList<>(m);
            for (int i = 0; i < m; i++) 
                ret.add(memo[i]);
            ready = true;
            synchronized (this) {
                notify();
            }
            return;
        }
        for (int i = from; i < n && !done; i++) {
            if (!vis[i]) {
                vis[i] = true;
                memo[depth] = list.get(i);
                dfs(depth + 1, i + 1);
                vis[i] = false;
            }
        }
    }

    public ArrayList<T> get() throws InterruptedException {
        if (done) return null;
        wanted = true;
        synchronized (lock) {
            lock.notify();
        }
        synchronized (this) {
            while (!ready && !done) wait();
        }
        ready = false;
        return ret;
    }

}
