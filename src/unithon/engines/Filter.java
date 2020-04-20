package unithon.engines;

@FunctionalInterface
public interface Filter<T> {
    boolean doFilter(T origin);
}
