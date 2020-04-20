package unithon.spider;

@FunctionalInterface
public interface Filter<T> {
    boolean doFilter(T origin);
}
