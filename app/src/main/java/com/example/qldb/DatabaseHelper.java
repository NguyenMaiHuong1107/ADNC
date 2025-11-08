package com.example.qldb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
    private static final int DATABASE_VERSION = 3;

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
                "num_people INTEGER NOT NULL CHECK (num_people > 0)," +
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

        // Thêm 2 bàn mẫu
        db.execSQL("INSERT INTO Tables (table_name, capacity, status) VALUES ('Table 1', 4, 'available')");
        db.execSQL("INSERT INTO Tables (table_name, capacity, status) VALUES ('Table 2', 6, 'available')");
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
        db.close();
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
        db.close();
        return u;
    }

    // ===========================================
    // KHU VỰC HÀM CHO RESERVATIONS (ĐẶT CHỖ)
    // ===========================================

    /**
     * Thêm 1 đơn đặt chỗ trạng thái PENDING, đồng thời ghi log vào Reservation_History.
     *
     * @param userId           người đặt
     * @param tableId          có thể null (chưa gán bàn)
     * @param reservationDate  ngày (khuyến nghị dạng dd/MM/yyyy)
     * @param timeSlot         giờ (HH:mm)
     * @param numPeople        tổng số khách (NL + TE)
     * @param notes            ghi chú của khách
     * @return reservation_id mới tạo
     */
    public long insertReservationPending(int userId,
                                         Integer tableId,
                                         String reservationDate,
                                         String timeSlot,
                                         int numPeople,
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
            cv.put("num_people", numPeople);
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
            db.close();
        }
        return newId;
    }

    /**
     * Cập nhật trạng thái đơn và ghi log lịch sử.
     *
     * @param reservationId   id đơn
     * @param newStatus       trạng thái mới (PENDING/CONFIRMED/CANCELLED/COMPLETED)
     * @param changedByUserId ai thực hiện thay đổi (user/admin); có thể null
     */
    public void updateReservationStatus(long reservationId,
                                        ReservationStatus newStatus,
                                        Integer changedByUserId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1) Update trạng thái trong bảng Reservations
            ContentValues cv = new ContentValues();
            cv.put("status", newStatus.getValue());

            // Nếu là CONFIRMED, lưu thêm người & thời điểm xác nhận
            if (newStatus == ReservationStatus.CONFIRMED && changedByUserId != null) {
                cv.put("confirmed_by", changedByUserId);
                // Ghi thời điểm xác nhận (ở đây demo là dùng millis -> INTEGER; bạn có thể chuyển sang CURRENT_TIMESTAMP nếu thích TEXT)
                cv.put("confirmed_at", System.currentTimeMillis());
            }

            db.update("Reservations", cv, "reservation_id=?",
                    new String[]{ String.valueOf(reservationId) });

            // 2) Ghi history (cần user_id chủ đơn)
            int ownerUserId = getReservationUserIdUnsafe(db, reservationId);
            insertReservationHistoryInternal(
                    db,
                    (int) reservationId,
                    ownerUserId,
                    newStatus.getValue(),
                    changedByUserId
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Lấy danh sách đơn đặt chỗ theo user_id (mới nhất trước).
     */
    public List<ReservationModel> getReservationsByUser(int userId) {
        List<ReservationModel> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT reservation_id, user_id, table_id, reservation_date, time_slot, num_people, status, notes " +
                        "FROM Reservations WHERE user_id=? ORDER BY reservation_id DESC",
                new String[]{ String.valueOf(userId) });

        while (c.moveToNext()) {
            long id = c.getLong(0);
            String date = c.getString(3);          // reservation_date
            String time = c.getString(4);          // time_slot
            int people = c.getInt(5);              // tổng số người (adult + child)
            String statusStr = c.getString(6);     // trạng thái
            String notes = c.getString(7);         // ghi chú (dùng tạm cho contactName)

            // ⚡ Giả định toàn bộ là người lớn, không có trẻ em — hoặc chia tạm
            int adult = people;
            int child = 0;

            // ⚡ Nếu bạn muốn hiển thị số điện thoại/tên người đặt, có thể JOIN bảng Users để lấy, tạm thời để trống
            String phone = "";
            String contactName = "";

            out.add(new ReservationModel(
                    id,
                    date,
                    time,
                    adult,
                    child,
                    phone,
                    contactName,
                    ReservationStatus.fromValue(statusStr)
            ));
        }

        c.close();
        db.close();
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
}
