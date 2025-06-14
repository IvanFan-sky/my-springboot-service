package com.spark.demo.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 * @author spark
 * @date 2025-06-14
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页显示条数", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "总页数", example = "10")
    private Long pages;

    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    @Schema(description = "是否为第一页", example = "true")
    private Boolean isFirst;

    @Schema(description = "是否为最后一页", example = "false")
    private Boolean isLast;

    /**
     * 私有构造函数
     */
    private PageResult() {}

    /**
     * 从MyBatis-Plus的Page对象创建分页结果
     * @param page MyBatis-Plus分页对象
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setHasPrevious(page.hasPrevious());
        result.setHasNext(page.hasNext());
        result.setIsFirst(page.getCurrent() == 1);
        result.setIsLast(page.getCurrent() >= page.getPages() || page.getPages() == 0);
        return result;
    }

    /**
     * 创建空的分页结果
     * @param current 当前页码
     * @param size 每页显示条数
     * @param <T> 数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(List.of());
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(0L);
        result.setPages(0L);
        result.setHasPrevious(false);
        result.setHasNext(false);
        result.setIsFirst(true);
        result.setIsLast(true);
        return result;
    }

    /**
     * 创建自定义分页结果
     * @param records 数据列表
     * @param current 当前页码
     * @param size 每页显示条数
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long current, Long size, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        
        // 计算总页数
        Long pages = (total + size - 1) / size;
        result.setPages(pages);
        
        // 计算分页状态
        result.setHasPrevious(current > 1);
        result.setHasNext(current < pages);
        result.setIsFirst(current == 1);
        result.setIsLast(current.equals(pages));
        
        return result;
    }

    /**
     * 获取数据条数
     * @return 当前页数据条数
     */
    public int getRecordCount() {
        return records != null ? records.size() : 0;
    }

    /**
     * 判断是否为空结果
     * @return 是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 判断是否有数据
     * @return 是否有数据
     */
    public boolean hasData() {
        return !isEmpty();
    }
} 