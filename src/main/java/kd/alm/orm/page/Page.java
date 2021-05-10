package kd.alm.orm.page;

import kd.bos.orm.query.QFilter;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/10 11:52
 */
@Data
@ToString
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 4461822179264122968L;
    /**
     * 页码
     */
    private int page;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 当前页条数
     */
    private int size;
    /**
     * 总条数
     */
    private int totalSize;
    /**
     * 下一页
     */
    private int nextPage;
    /**
     * 上一页
     */
    private int pervPage;
    /**
     * 数据
     */
    private List<T> data;
    /**
     * 排序字段
     */
    private String orderBy;
    /**
     * 排序方式
     */
    private Sorter sorter;

    @SafeVarargs
    public final void handleSort(Comparator<? super T>... comparators) {
        Stream<T> stream = data.stream();
        for (Comparator<? super T> comparator : comparators) {
            stream = stream.sorted(comparator);
        }
        data = stream.collect(Collectors.toList());
    }

    /**
     * 生成分页结果
     *
     * @param totalSize   总条数
     * @param pageRequest 分页请求
     * @param <T>         泛型
     * @return 分页结果对象
     */
    public static <T> Page<T> buildPage(int totalSize, PageRequest pageRequest) {
        // 计算总页数
        double totalPage = Math.ceil((double) totalSize / pageRequest.getSize());
        // 下一页
        int nextPage;
        // 上一页
        int pervPage;

        if (pageRequest.getPage() >= totalPage) {
            nextPage = -1;
            pervPage = -1;
        } else {
            // 计算分页前/后页码
            pervPage = pageRequest.getPage() - 1;
            nextPage = pageRequest.getPage() + 1;
            if (nextPage == totalPage) {
                nextPage = -1;
            }
        }

        final Page<T> page = new Page<>();
        // 结果
        page.setNextPage(nextPage);
        page.setPervPage(pervPage);
        page.setSize(pageRequest.getSize());
        page.setPage(pageRequest.getPage());
        page.setTotalPage((int) totalPage);
        page.setTotalSize(totalSize);
        page.setData(new ArrayList<>());
        return page;
    }

}
