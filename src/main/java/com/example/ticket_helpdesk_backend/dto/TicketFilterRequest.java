package com.example.ticket_helpdesk_backend.dto;

import java.util.Map;

public class TicketFilterRequest {
    private Map<String, Object> filters; // dynamic filter: status, priority, assignee...
    private SortDTO sort;                // sort field + order
    private PaginationDTO pagination;    // page + size

    // Constructors
    public TicketFilterRequest() {}

    public TicketFilterRequest(Map<String, Object> filters, SortDTO sort, PaginationDTO pagination) {
        this.filters = filters;
        this.sort = sort;
        this.pagination = pagination;
    }

    // Getters & Setters
    public Map<String, Object> getFilters() {
        return filters;
    }
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public SortDTO getSort() {
        return sort;
    }
    public void setSort(SortDTO sort) {
        this.sort = sort;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }
    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
}

