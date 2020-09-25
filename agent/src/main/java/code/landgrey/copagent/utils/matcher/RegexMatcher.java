package code.landgrey.copagent.utils.matcher;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/core/src/main/java/com/taobao/arthas/core/util/matcher/RegexMatcher.java
 *
 */
public class RegexMatcher implements Matcher<String> {

    private final String pattern;

    public RegexMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matching(String target) {
        return null != target
                && null != pattern
                && target.matches(pattern);
    }
}
