package code.landgrey.copagent.utils.matcher;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/core/src/main/java/com/taobao/arthas/core/util/matcher/Matcher.java
 *
 */
public interface Matcher<T> {

    /**
     * 是否匹配
     *
     * @param target 目标字符串
     * @return 目标字符串是否匹配表达式
     */
    boolean matching(T target);

}
