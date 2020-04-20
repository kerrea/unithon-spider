package unithon.spider;

import java.net.URL;

@FunctionalInterface
public interface AnchorFilter {
    boolean doFilter(URL origin);
}
