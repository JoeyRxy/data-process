package rxy.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombIter<T> implements Iterator<ArrayList<T>> {

    private final ArrayList<T> list;
    private final int m;
    private final int n;
    private int[] idx;
    private boolean hasNext = true;
    private boolean reverse;

    public CombIter(List<T> list, int m) {
        if (list instanceof ArrayList)
            this.list = (ArrayList<T>) list;
        else
            this.list = new ArrayList<>(list);
        this.n = list.size();
        if (n < m) {
            this.m = m;
            hasNext = false;
            return;
        }
        // reverse = (m > (n >> 1));
        if (reverse) {
            this.m = n - m;
            idx = new int[this.m];
            int j = n;
            for (int i = this.m - 1; i > -1; --i)
                idx[i] = --j;
        } else {
            this.m = m;
            idx = new int[m];
            for (int i = 0; i < m; ++i)
                idx[i] = i;
        }
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ArrayList<T> next() {
        if (!hasNext)
            return null;
        ArrayList<T> ret = new ArrayList<>(idx.length);
        hasNext = false;
        if (reverse) {
            Iterator<T> iter = list.iterator();
            int j = 0;
            for (int i : idx) {
                while (j++ < i)
                    ret.add(iter.next());
                iter.next();
            }
            while (iter.hasNext())
                ret.add(iter.next());
            
            int i;
            for (i = m - 1; i > 0; --i) {
                if (idx[i] > 1 + idx[i - 1]) {
                    hasNext = true;
                    --idx[i];
                    if (idx[m - 1] != n - 1) {
                        int _j = n;
                        for (int k = m - 1; k > i; --k) 
                            idx[k] = --_j;
                    }
                    break;
                }
            }
            if (i == 0) {
                hasNext = (--idx[0] >= 0);
                if (hasNext && idx[m - 1] != n - 1) {
                    int _j = n;
                    for (i = m - 1; i > 0; --i) 
                        idx[i] = --_j;
                }
            }
            // System.out.println(Arrays.toString(idx));
        } else {
            for (int _i : idx)
                ret.add(list.get(_i));

            for (int i = 0; i < m; ++i) {
                int t = idx[m - i - 1];
                if (t < n - i - 1) {
                    hasNext = true;
                    idx[m - i - 1] = (++t);
                    for (int j = m - i; j < m; ++j)
                        idx[j] = (++t);
                    break;
                }
            }
        }
        return ret;
    }

}
