package unithon.spider;

@FunctionalInterface
public interface ContentFilter {
    boolean doFilter(String origin);
}
