package com.example.ticket_helpdesk_backend.dto;

public class PaginationDTO {
    private int page = 1; // Trang hiện tại, mặc định 1
    private int size = 20; // Số item mỗi trang, mặc định 20

    // Constructors
    public PaginationDTO() {}
    public PaginationDTO(int page, int size) {
        this.page = page;
        this.size = size;
    }

    // Getters & Setters
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}

