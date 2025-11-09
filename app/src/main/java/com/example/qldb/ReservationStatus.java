package com.example.qldb;

// Enum cho trạng thái của bảng Reservations
public enum ReservationStatus {
    // ⭐️ CHỈ CẦN THÊM ON_SITE VÀO DANH SÁCH NÀY
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed"),
    ON_SITE("on_site"); // ⭐️ ĐÃ THÊM

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
        // Có thể trả về null hoặc một giá trị mặc định thay vì ném ra lỗi
        // throw new IllegalArgumentException("Invalid ReservationStatus value: " + value);
        return null;
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
            case ON_SITE: // ⭐️ THÊM CASE CHO ON_SITE
                return "Đang ăn tại quán";
            default:
                return "Không xác định";
        }
    }
}