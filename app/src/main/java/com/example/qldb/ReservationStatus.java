package com.example.qldb;

// Enum cho trạng thái của bảng Reservations
public enum ReservationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Chuyển đổi từ chuỗi sang enum
    public static ReservationStatus fromValue(String value) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ReservationStatus value: " + value);
    }
    public String getDisplayName() {
        switch (this) {
            case PENDING:
                return "Chờ xác nhận";
            case CONFIRMED:
                return "Đã xác nhận";
            case CANCELLED:
                return "Đã hủy";
            case COMPLETED:
                return "Hoàn thành";
            default:
                return "Không xác định";
        }
    }
}


