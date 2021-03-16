package rxy.process;

import java.util.Objects;

public class Tuple<T1, T2> {
    T1 t1;
    T2 t2;

    public Tuple(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        Tuple other = (Tuple) obj;
        return Objects.equals(t1, other.t1) && Objects.equals(t2, other.t2);
    }

    @Override
    public String toString() {
        return "(" + t1 + ", " + t2 + ")";
    }

}
