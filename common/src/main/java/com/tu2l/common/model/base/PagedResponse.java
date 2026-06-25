package com.tu2l.common.model.base;

import com.tu2l.common.model.states.ResponseProcessingStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Generic paginated response wrapper. Exposes a stable pagination contract
 * (page index, size, totals) instead of leaking a paging library's internal
 * serialization. Built from primitives so {@code common} stays free of any
 * Spring Data dependency.
 *
 * @param <T> type of the items in {@link #content}
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PagedResponse<T> extends BaseResponse {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        PagedResponse<T> r = new PagedResponse<>();
        r.setContent(content);
        r.setPage(page);
        r.setSize(size);
        r.setTotalElements(totalElements);
        r.setTotalPages(totalPages);
        r.setStatus(ResponseProcessingStatus.SUCCESS);
        return r;
    }
}
