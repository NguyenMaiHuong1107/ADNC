package com.example.qldb;

// Enum cho trạng thái của bảng Tables
public enum TableStatus {
    AVAILABLE("available"),
    OCCUPIED("occupied"),
    RESERVED("reserved");

    private final String value;

    TableStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Chuyển đổi từ chuỗi sang enum
    public static TableStatus fromValue(String value) {
        for (TableStatus status : TableStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid TableStatus value: " + value);
    }
    public String getDisplayName() {
        switch (this) {
            case AVAILABLE:
                return "Trống";
            case OCCUPIED:
                return "Đang phục vụ";
            case RESERVED:
                return "Đã đặt";
            default:
                return "Không xác định";
        }
    }
}
