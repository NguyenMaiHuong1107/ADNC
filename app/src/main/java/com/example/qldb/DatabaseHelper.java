package com.example.qldb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp trợ giúp làm việc với SQLite:
 * - Tạo CSDL & các bảng Users, Tables, Reservations, Reservation_History
 * - Cung cấp hàm đăng ký/đăng nhập User
 * - Cung cấp hàm thêm đơn đặt chỗ (mặc định PENDING), cập nhật trạng thái & ghi log lịch sử
 * - Cung cấp hàm lấy lịch sử đặt chỗ theo user
 *
 * Chú ý:
 * - Nếu thay đổi cấu trúc CSDL, tăng DATABASE_VERSION và xử lý onUpgrade() phù hợp.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên file CSDL & phiên bản
    private static final String DATABASE_NAME = "restaurant.db";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_RESERVATIONS = "Reservations";
    private static final String COLUMN_TABLE_STATUS = "status"; // Tên cột status trong bảng Tables
    private static final String TABLE_TABLES = "Tables";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Bật ràng buộc khóa ngoại (FOREIGN KEY).
     * SQLite chỉ thật sự bật FK nếu gọi setForeignKeyConstraintsEnabled(true).
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Tạo các bảng ngay lần đầu CSDL được khởi tạo.
     *  - Users: thông tin người dùng
     *  - Tables: thông tin bàn (sức chứa, trạng thái)
     *  - Reservations: đơn đặt chỗ
     *  - Reservation_History: log thay đổi trạng thái đơn
     *
     * Đồng thời chèn sẵn 2 user mẫu (admin & 1 user) + 2 bàn mẫu.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng Users: lưu tài khoản
        db.execSQL("CREATE TABLE Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "phone TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "role TEXT NOT NULL DEFAULT 'user'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        // Bảng Tables: quản lý bàn trong nhà hàng
        db.execSQL("CREATE TABLE Tables (" +
                "table_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "table_name TEXT," +
                "capacity INTEGER NOT NULL CHECK (capacity > 0)," +
                "status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available', 'occupied', 'reserved'))," +
                "last_updated_by INTEGER," +
                "last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (last_updated_by) REFERENCES Users(user_id))");

        // Bảng Reservations: đơn đặt chỗ
        // Lưu ý: reservation_date/time_slot đang dùng TEXT cho dễ hiển thị. Nếu cần lọc/sort thời gian chuẩn, cân nhắc ISO "yyyy-MM-dd" hoặc epoch millis (INTEGER)
        db.execSQL("CREATE TABLE Reservations (" +
                "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +            // ai đặt đơn
                "table_id INTEGER," +                    // có thể null (chưa gán bàn)
                "reservation_date TEXT NOT NULL," +      // dạng đề xuất: dd/MM/yyyy
                "time_slot TEXT NOT NULL," +             // dạng đề xuất: HH:mm
                "num_adults INTEGER NOT NULL DEFAULT 1," +    // ⭐️ THÊM DÒNG NÀY
                "num_children INTEGER NOT NULL DEFAULT 0," +  // ⭐️ THÊM DÒNG NÀY
                "status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'cancelled', 'completed'))," +
                "notes TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "confirmed_by INTEGER," +
                "confirmed_at TIMESTAMP," +              // có thể lưu CURRENT_TIMESTAMP hoặc millis
                "FOREIGN KEY (user_id) REFERENCES Users(user_id)," +
                "FOREIGN KEY (table_id) REFERENCES Tables(table_id)," +
                "FOREIGN KEY (confirmed_by) REFERENCES Users(user_id))");

        // Bảng Reservation_History: lưu mỗi lần đổi trạng thái
        db.execSQL("CREATE TABLE Reservation_History (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reservation_id INTEGER NOT NULL," +   // đơn nào
                "user_id INTEGER NOT NULL," +          // chủ đơn (để truy vết lịch sử theo user)
                "status_change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "new_status TEXT NOT NULL CHECK (new_status IN ('pending', 'confirmed', 'cancelled', 'completed'))," +
                "changed_by INTEGER," +                // ai thay đổi trạng thái (user/admin), có thể null
                "FOREIGN KEY (reservation_id) REFERENCES Reservations(reservation_id)," +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id)," +
                "FOREIGN KEY (changed_by) REFERENCES Users(user_id))");

        // (Khuyến nghị) Tạo index giúp truy vấn nhanh hơn
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reservations_user ON Reservations(user_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_history_res ON Reservation_History(reservation_id)");

        // Thêm 2 user mẫu: admin & user thường (mật khẩu 123456 đã băm)
        String adminPassword = hashPassword("123456");
        String userPassword  = hashPassword("123456");
        db.execSQL("INSERT INTO Users (full_name, phone, password_hash, role) " +
                "VALUES ('Admin', '0999999999', '" + adminPassword + "', 'admin')");
        db.execSQL("INSERT INTO Users (full_name, phone, password_hash, role) " +
                "VALUES ('Nguyễn Mai Hương', '0123456789', '" + userPassword + "', 'user')");

        for (int i = 1; i <= 30; i++) {
            ContentValues tableValues = new ContentValues();
            tableValues.put("table_name", "Bàn " + i);
            tableValues.put("capacity", 4); // Bạn có thể đặt capacity mặc định
            tableValues.put("status", "available");
            db.insert("Tables", null, tableValues); // "Tables" là tên bảng của bạn
        }
    }

    /**
     * Nâng cấp CSDL khi tăng DATABASE_VERSION.
     * Demo hiện tại: drop & tạo lại (sẽ mất dữ liệu).
     * Triển khai thật: nên ALTER TABLE để giữ dữ liệu.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Reservation_History");
        db.execSQL("DROP TABLE IF EXISTS Reservations");
        db.execSQL("DROP TABLE IF EXISTS Tables");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    // ==============================
    // KHU VỰC HÀM CHO USERS (TÀI KHOẢN)
    // ==============================

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa.
     * @return true nếu đã có trong bảng Users.
     */
    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "Users",
                new String[]{"user_id"},
                "phone = ?",
                new String[]{phone},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Thêm user mới.
     * @param passwordHash: chuỗi mật khẩu đã băm SHA-256 (xem hashPassword)
     * @return rowId (user_id) nếu thành công, -1 nếu lỗi.
     */
    public long insertUser(String fullName, String phone, String passwordHash, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        values.put("password_hash", passwordHash);
        values.put("role", role);
        long result = db.insert("Users", null, values);
        return result;
    }

    /**
     * Kiểm tra đăng nhập bằng phone + password_hash.
     */
    public boolean checkUser(String phone, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "Users",
                new String[]{"user_id"},
                "phone = ? AND password_hash = ?",
                new String[]{phone, passwordHash},
                null, null, null
        );
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    /**
     * Băm mật khẩu SHA-256 (dùng khi insert default users).
     * Ứng dụng thực tế: hãy thêm SALT + lặp, hoặc dùng thư viện chuyên biệt.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // fallback (không nên dùng ở môi trường thật)
        }
    }

    /**
     * Lấy user_id nếu phone + password_hash khớp.
     * @return user_id hoặc -1 nếu không tìm thấy.
     */
    public int getUserId(String phone, String hashedPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT user_id FROM Users WHERE phone=? AND password_hash=?",
                new String[]{phone, hashedPassword}
        );
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    /**
     * Lấy thông tin tối thiểu theo user_id (id, full_name, phone).
     */
    public UserModel getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT user_id, full_name, phone FROM Users WHERE user_id = ?",
                new String[]{ String.valueOf(userId) }
        );
        UserModel u = null;
        if (c.moveToFirst()) {
            u = new UserModel(c.getInt(0), c.getString(1), c.getString(2));
        }
        c.close();
        return u;
    }



    public long insertReservationPending(int userId,
                                         Integer tableId,
                                         String reservationDate,
                                         String timeSlot,
                                         int numAdults,   // Tham số ĐÚNG
                                         int numChildren, // Tham số ĐÚNG
                                         String notes) {
        SQLiteDatabase db = getWritableDatabase();
        long newId;
        db.beginTransaction();
        try {
            // 1) Insert vào bảng Reservations
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            if (tableId != null) cv.put("table_id", tableId);
            cv.put("reservation_date", reservationDate);
            cv.put("time_slot", timeSlot);

            // cv.put("num_people", numPeople); // ⭐️ XÓA DÒNG NÀY
            cv.put("num_adults", numAdults);     // ⭐️ THÊM DÒNG NÀY
            cv.put("num_children", numChildren); // ⭐️ THÊM DÒNG NÀY

            cv.put("status", ReservationStatus.PENDING.getValue());
            cv.put("notes", notes);
            newId = db.insertOrThrow("Reservations", null, cv);

            // 2) Ghi history: trạng thái PENDING
            insertReservationHistoryInternal(
                    db,
                    (int) newId,
                    userId,                                 // chủ đơn
                    ReservationStatus.PENDING.getValue(),   // trạng thái mới
                    userId                                  // changed_by: người thực hiện (chính user)
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return newId;
    }


    // ⭐️ THÊM HÀM MỚI NÀY (Hàm private hỗ trợ)
    /**
     * Tìm bàn "available" ĐẦU TIÊN, cập nhật trạng thái của nó sang "reserved",
     * và trả về ID của bàn đó.
     * CHỈ NÊN GỌI TỪ BÊN TRONG MỘT TRANSACTION.
     *
     * @param db Đối tượng SQLiteDatabase đang trong transaction.
     * @return table_id nếu gán thành công, -1 nếu hết bàn trống.
     */
    private int assignFirstAvailableTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT table_id FROM " + TABLE_TABLES +
                        " WHERE " + COLUMN_TABLE_STATUS + " = 'available' LIMIT 1",
                null
        );

        int tableId = -1;
        if (cursor.moveToFirst()) {
            tableId = cursor.getInt(0);
        }
        cursor.close();

        if (tableId != -1) {
            // Đã tìm thấy bàn. Cập nhật trạng thái của bàn đó sang "reserved"
            ContentValues tableCv = new ContentValues();
            tableCv.put(COLUMN_TABLE_STATUS, "reserved"); // Hoặc "occupied" tùy logic của bạn
            db.update(TABLE_TABLES, tableCv, "table_id = ?", new String[]{String.valueOf(tableId)});
        }
        return tableId; // Trả về -1 nếu không tìm thấy bàn nào (hết bàn)
    }


    public boolean updateReservationStatus(int reservationId, ReservationStatus newStatus, int adminId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // ⭐️ TRƯỜNG HỢP 1: Nếu trạng thái mới KHÔNG PHẢI LÀ "CONFIRMED"
        // (Ví dụ: Hủy, Đã ăn xong, v.v. - không cần chiếm bàn)
        if (newStatus != ReservationStatus.CONFIRMED) {
            ContentValues values = new ContentValues();
            values.put("status", newStatus.getValue());   // ⭐️ SỬA: "status"
            values.put("confirmed_by", adminId);          // ⭐️ SỬA: "confirmed_by"

            db.update(TABLE_RESERVATIONS, values, "reservation_id = ?", new String[]{String.valueOf(reservationId)}); // ⭐️ SỬA: "reservation_id"
            // Vì không cần chiếm bàn, nên luôn thành công
            return true;
        }

        // ⭐️ TRƯỜNG HỢP 2: Trạng thái mới là "CONFIRMED" - Logic quan trọng nhất
        db.beginTransaction();
        try {
            // Bước 1: Tìm một bàn trống (status = "available")
            Cursor cursor = db.query(TABLE_TABLES,
                    new String[]{"table_id"},                       // ⭐️ SỬA: "table_id"
                    COLUMN_TABLE_STATUS + " = ?",                   // ⭐️ SỬA: COLUMN_TABLE_STATUS (bạn có định nghĩa biến này)
                    new String[]{"available"},                      // là "available"
                    null, null, null,
                    "1");                                           // Chỉ lấy 1 bàn

            String availableTableId = null;
            if (cursor != null && cursor.moveToFirst()) {
                availableTableId = cursor.getString(cursor.getColumnIndexOrThrow("table_id")); // ⭐️ SỬA: "table_id"
                cursor.close();
            }

            // Bước 2: Kiểm tra xem có tìm được bàn không
            if (availableTableId == null) {
                // KHÔNG CÒN BÀN TRỐNG!
                Log.w("DatabaseHelper", "Confirm failed: No available tables.");
                return false; // Báo cho Fragment biết là đã thất bại
            }

            // Bước 3: Vẫn còn bàn. Cập nhật bảng Reservations
            ContentValues reservationValues = new ContentValues();
            reservationValues.put("status", ReservationStatus.CONFIRMED.getValue()); // ⭐️ SỬA: "status"
            reservationValues.put("confirmed_by", adminId);                        // ⭐️ SỬA: "confirmed_by"
            reservationValues.put("table_id", availableTableId);                   // ⭐️ SỬA: "table_id"

            db.update(TABLE_RESERVATIONS, reservationValues, "reservation_id = ?", new String[]{String.valueOf(reservationId)}); // ⭐️ SỬA: "reservation_id"

            // Bước 4: Cập nhật bảng Tables (Chuyển bàn từ "available" -> "occupied")
            ContentValues tableValues = new ContentValues();
            // (Lưu ý: status của bảng Tables là "occupied" - bị chiếm, khác với "confirmed" của đơn hàng)
            tableValues.put(COLUMN_TABLE_STATUS, "occupied"); // ⭐️ SỬA: COLUMN_TABLE_STATUS và "occupied"

            db.update(TABLE_TABLES, tableValues, "table_id = ?", new String[]{availableTableId}); // ⭐️ SỬA: "table_id"

            // Bước 5: Đánh dấu transaction thành công
            db.setTransactionSuccessful();
            return true; // Báo thành công

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error during confirm transaction", e);
            return false; // Báo thất bại
        } finally {
            db.endTransaction(); // Hoàn tất transaction (commit hoặc rollback)
        }
    }    /**
     * Lấy danh sách đơn đặt chỗ theo user_id (mới nhất trước).
     */
// ⭐️ THAY THẾ HÀM NÀY
    public List<ReservationModel> getReservationsByUser(int userId) {
        List<ReservationModel> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // ⭐️ SỬA QUERY: Lấy num_adults, num_children
        Cursor c = db.rawQuery(
                "SELECT reservation_id, user_id, table_id, reservation_date, time_slot, " +
                        "num_adults, num_children, status, notes " + // ⭐️ SỬA Ở ĐÂY
                        "FROM Reservations WHERE user_id=? ORDER BY reservation_id DESC",
                new String[]{ String.valueOf(userId) });

        while (c.moveToNext()) {
            long id = c.getLong(0);
            String date = c.getString(3);
            String time = c.getString(4);

            // ⭐️ SỬA INDEX
            int adult = c.getInt(5);
            int child = c.getInt(6);
            String statusStr = c.getString(7);
            String notes = c.getString(8);

            // ⭐️ Lấy thông tin user (tạm thời)
            UserModel u = getUserById(userId);
            String phone = (u != null) ? u.phone : "";
            String contactName = (u != null) ? u.fullName : "";

            out.add(new ReservationModel(
                    id, date, time, adult, child, phone, contactName,
                    ReservationStatus.fromValue(statusStr)
            ));
        }
        c.close();
        return out;
    }
    // ==============================
    // HÀM NỘI BỘ (PRIVATE HELPERS)
    // ==============================

    /**
     * Ghi 1 dòng vào Reservation_History.
     * @param userIdOwner  chủ sở hữu đơn (để sau này lọc lịch sử theo user)
     * @param changedBy    ai thực hiện thay đổi (có thể null)
     */
    private void insertReservationHistoryInternal(SQLiteDatabase db,
                                                  int reservationId,
                                                  int userIdOwner,
                                                  String newStatus,
                                                  Integer changedBy) {
        ContentValues h = new ContentValues();
        h.put("reservation_id", reservationId);
        h.put("user_id", userIdOwner);
        h.put("new_status", newStatus);
        if (changedBy != null) h.put("changed_by", changedBy);
        db.insert("Reservation_History", null, h); // status_change_date = CURRENT_TIMESTAMP tự động
    }

    /**
     * Lấy user_id của chủ đơn từ bảng Reservations (dùng khi ghi history).
     * Không đóng DB vì được gọi trong transaction của updateReservationStatus.
     */
    private int getReservationUserIdUnsafe(SQLiteDatabase db, long reservationId) {
        Cursor c = db.rawQuery(
                "SELECT user_id FROM Reservations WHERE reservation_id=?",
                new String[]{ String.valueOf(reservationId) });
        int uid = -1;
        if (c.moveToFirst()) uid = c.getInt(0);
        c.close();
        return uid;
    }

    // Thêm 2 hàm này vào lớp DatabaseHelper.java của bạn

    /**
     * Lấy role (admin/user) của user_id.
     * @return "admin", "user", hoặc null nếu không tìm thấy.
     */
    public String getUserRole(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT role FROM Users WHERE user_id = ?",
                new String[]{ String.valueOf(userId) }
        );
        String role = null;
        if (c.moveToFirst()) {
            role = c.getString(0);
        }
        c.close();
        return role;
    }

    /**
     * Lấy TẤT CẢ đơn đặt chỗ (cho admin).
     * Bao gồm cả thông tin người đặt (JOIN với Users).
     */
// ⭐️ THAY THẾ HÀM NÀY
    public List<ReservationModel> getAllReservations() {
        List<ReservationModel> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT r.reservation_id, r.user_id, r.table_id, r.reservation_date, " +
                "r.time_slot, " +
                "r.num_adults, r.num_children, " + // ⭐️ SỬA Ở ĐÂY
                "r.status, r.notes, u.full_name, u.phone " +
                "FROM Reservations r " +
                "JOIN Users u ON r.user_id = u.user_id " +
                "ORDER BY r.reservation_id DESC";

        Cursor c = db.rawQuery(query, null);

        while (c.moveToNext()) {
            long id = c.getLong(0);
            String date = c.getString(3);
            String time = c.getString(4);

            // ⭐️ SỬA INDEX
            int adult = c.getInt(5);
            int child = c.getInt(6);
            String statusStr = c.getString(7);
            String notes = c.getString(8);
            String contactName = c.getString(9);
            String phone = c.getString(10);

            out.add(new ReservationModel(
                    id, date, time, adult, child, phone, contactName,
                    ReservationStatus.fromValue(statusStr)
            ));
        }
        c.close();
        return out;
    }
    // Thêm 3 hàm này vào DatabaseHelper.java

    /**
     * Đếm số lượng đơn theo trạng thái (cho admin dashboard)
     */
    public int getCountOfReservationsByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Reservations WHERE status = ?",
                new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Đếm số lượng bàn theo trạng thái (cho admin dashboard)
     */
    public int getCountOfTablesByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Tables WHERE status = ?",
                new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Thêm hàm này vào file DatabaseHelper.java

    /**
     * Lấy danh sách đơn đặt chỗ dựa trên một LIST các trạng thái.
     */
    public List<ReservationModel> getReservationsByStatusList(List<String> statuses) {
        List<ReservationModel> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Xây dựng mệnh đề WHERE IN (...) cho linh hoạt
        // Ví dụ: "WHERE r.status IN (?, ?)"
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < statuses.size(); i++) {
            whereClause.append("?");
            if (i < statuses.size() - 1) {
                whereClause.append(",");
            }
        }
        String[] statusArray = statuses.toArray(new String[0]);

        // ⭐️ SỬA QUERY: Lấy num_adults và num_children
        String query = "SELECT r.reservation_id, r.user_id, r.table_id, r.reservation_date, " +
                "r.time_slot, " +
                "r.num_adults, r.num_children, " + // ⭐️ THAY VÌ num_people
                "r.status, r.notes, u.full_name, u.phone " +
                "FROM Reservations r " +
                "JOIN Users u ON r.user_id = u.user_id " +
                "WHERE r.status IN (" + whereClause.toString() + ") " +
                "ORDER BY r.reservation_id DESC";

        Cursor c = db.rawQuery(query, statusArray);

        while (c.moveToNext()) {
            long id = c.getLong(0);
            String date = c.getString(3);
            String time = c.getString(4);

            // ⭐️ SỬA INDEX: Lấy 2 cột riêng biệt
            int adult = c.getInt(5); // index 5 là num_adults
            int child = c.getInt(6); // index 6 là num_children

            // ⭐️ SỬA INDEX: Các cột sau bị dịch đi 1
            String statusStr = c.getString(7);
            String notes = c.getString(8);
            String contactName = c.getString(9);
            String phone = c.getString(10);

            // ⭐️ Đưa thẳng vào model
            out.add(new ReservationModel(
                    id, date, time, adult, child, phone, contactName,
                    ReservationStatus.fromValue(statusStr)
            ));
        }
        c.close();
        return out;
    }
    // Thêm hàm này vào file DatabaseHelper.java

    /**
     * Xóa vĩnh viễn một đơn đặt chỗ và lịch sử liên quan.
     * Dùng cho admin khi muốn xóa hẳn đơn.
     */
    public void deleteReservation(long reservationId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1) Xóa các mục trong lịch sử trước (vì có khóa ngoại)
            db.delete(
                    "Reservation_History",
                    "reservation_id = ?",
                    new String[]{ String.valueOf(reservationId) }
            );

            // 2) Xóa đơn đặt chỗ chính
            db.delete(
                    "Reservations",
                    "reservation_id = ?",
                    new String[]{ String.valueOf(reservationId) }
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    // ⭐️ THÊM HÀM MỚI NÀY VÀO CUỐI FILE
    /**
     * Lấy danh sách TẤT CẢ người dùng (không phải admin).
     * Dùng cho trang "Người dùng" của Admin.
     */
    public List<UserModel> getAllUsers() {
        List<UserModel> userList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Chỉ lấy những ai có role = 'user'
        Cursor c = db.rawQuery(
                "SELECT user_id, full_name, phone FROM Users WHERE role = 'user' ORDER BY full_name",
                null
        );

        while (c.moveToNext()) {
            UserModel u = new UserModel(c.getInt(0), c.getString(1), c.getString(2));
            userList.add(u);
        }
        c.close();
        return userList;
    }

    // Trong lớp DatabaseHelper.java

    // 1. Phương thức xóa tất cả các đơn đặt bàn (ĐÃ SỬA)
    public void clearAllReservations() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Bắt đầu transaction để đảm bảo cả 2 lệnh cùng thành công
        db.beginTransaction();
        try {
            // BƯỚC 1: Xóa lịch sử trước (vì có khóa ngoại)
            // (Giả sử tên bảng lịch sử của bạn là "Reservation_History"
            // dựa trên file đầy đủ bạn gửi trước đó)
            db.delete("Reservation_History", null, null);

            // BƯỚC 2: Xóa bảng Reservations chính
            db.delete(TABLE_RESERVATIONS, null, null);

            // Đánh dấu transaction thành công
            db.setTransactionSuccessful();
        } finally {
            // Kết thúc transaction (commit nếu successful, rollback nếu lỗi)
            db.endTransaction();
        }
    }

    // 2. Phương thức đặt lại (Hàm này đã ĐÚNG, không cần sửa)
    public void resetAllTablesToAvailable() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TABLE_STATUS, "available");

        // Cập nhật TẤT CẢ các bàn trong bảng tables
        db.update(TABLE_TABLES, values, null, null);

    }

    // Trong file DatabaseHelper.java
    // HÃY XÓA HÀM CŨ 'completeReservation' VÀ THAY BẰNG HÀM NÀY

    /**
     * Hoàn thành và XÓA một đơn đặt chỗ:
     * 1. Tìm bàn (table_id) đã gán cho đơn này.
     * 2. Cập nhật trạng thái bàn đó -> "available" (trả bàn).
     * 3. Xóa log lịch sử của đơn này (tránh lỗi khóa ngoại).
     * 4. Xóa vĩnh viễn đơn hàng này.
     *
     * @param reservationId ID của đơn hàng
     */
    public void completeReservation(long reservationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        int tableId = -1;

        try {
            // --- Bước 1: Lấy table_id từ đơn hàng ---
            Cursor c = db.rawQuery(
                    "SELECT table_id FROM " + TABLE_RESERVATIONS + " WHERE reservation_id = ?",
                    new String[]{String.valueOf(reservationId)}
            );
            if (c.moveToFirst()) {
                if (!c.isNull(0)) {
                    tableId = c.getInt(0); // Lấy ID bàn
                }
            }
            c.close();

            // --- Bước 2: Cập nhật trạng thái bàn về "available" ---
            if (tableId != -1) {
                ContentValues tableCv = new ContentValues();
                tableCv.put(COLUMN_TABLE_STATUS, "available");
                db.update(TABLE_TABLES, tableCv, "table_id = ?", new String[]{String.valueOf(tableId)});
            }

            // --- Bước 3: Xóa log lịch sử trước (vì có khóa ngoại) ---
            db.delete("Reservation_History", "reservation_id = ?", new String[]{String.valueOf(reservationId)});

            // --- Bước 4: Xóa vĩnh viễn đơn hàng ---
            db.delete(TABLE_RESERVATIONS, "reservation_id = ?", new String[]{String.valueOf(reservationId)});

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }


    public boolean createOnSiteBooking(String guestName, String guestPhone, int numGuests, int adminUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;

        try {
            // --- Bước 1: Tìm hoặc Tạo User ---
            int guestUserId = -1;
            Cursor c = db.query("Users", new String[]{"user_id"}, "phone = ?", new String[]{guestPhone}, null, null, null);

            if (c.moveToFirst()) {
                guestUserId = c.getInt(0); // User đã tồn tại
            }
            c.close();

            if (guestUserId == -1) {
                // User mới, tạo tài khoản
                ContentValues userCv = new ContentValues();
                userCv.put("full_name", guestName);
                userCv.put("phone", guestPhone);
                userCv.put("password_hash", hashPassword("123456")); // Mật khẩu mặc định
                userCv.put("role", "user");
                guestUserId = (int) db.insertOrThrow("Users", null, userCv);
            }

            if (guestUserId == -1) {
                // Không thể tạo user, rollback
                throw new Exception("Không thể tạo user");
            }

            // --- Bước 2: Gán bàn ---
            int tableId = assignFirstAvailableTable(db); // Hàm này đã set bàn thành 'reserved'
            if (tableId == -1) {
                // Hết bàn trống, rollback
                db.endTransaction();
                return false; // Báo hiệu thất bại
            }

            // --- Bước 3: Tạo Reservation (Đơn) ---
            ContentValues resCv = new ContentValues();
            resCv.put("user_id", guestUserId);
            resCv.put("table_id", tableId);

            // Lấy ngày giờ hiện tại
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = sdfDate.format(new Date());
            String currentTime = sdfTime.format(new Date());

            resCv.put("reservation_date", currentDate);
            resCv.put("time_slot", currentTime);
            resCv.put("num_adults", numGuests);
            resCv.put("num_children", 0);
            resCv.put("status", ReservationStatus.ON_SITE.getValue()); // ⭐️ Trạng thái MỚI
            resCv.put("notes", "Khách vãng lai");
            resCv.put("confirmed_by", adminUserId);
            resCv.put("confirmed_at", System.currentTimeMillis());

            long newResId = db.insertOrThrow(TABLE_RESERVATIONS, null, resCv);

            // --- Bước 4: Ghi History ---
            insertReservationHistoryInternal(db, (int) newResId, guestUserId, ReservationStatus.ON_SITE.getValue(), adminUserId);

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            db.endTransaction();
            // Không gọi db.close()
        }
        return success;
    }
}

