/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;

public class Main {

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost/data_karyawan", "yourusername", "yourpassword");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return con;
    }

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        Scanner scanner = new Scanner(System.in);
        int loggedInUserId = -1;

        try {
            conn = getConnection();
            if (conn == null) {
                System.out.println("Failed to establish connection.");
                return;
            }
            stmt = conn.createStatement();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            loggedInUserId = login(username, password, conn);

            if (loggedInUserId != -1) {
                if (username.equals("admin")) {
                    adminMenu(scanner, conn);
                } else {
                    userMenu(scanner, conn, loggedInUserId);
                }
            } else {
                System.out.println("Invalid username or password.");
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                scanner.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    static int login(String username, String password, Connection conn) {
        int userId = -1;
        try {
            String sql = "SELECT id FROM karyawan WHERE yourusername=? AND yourpassword=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("id");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return userId;
    }

    static void adminMenu(Scanner scanner, Connection conn) {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Input data karyawan");
            System.out.println("2. Edit data karyawan");
            System.out.println("3. Laporan absensi");
            System.out.println("4. Rekap absensi");
            System.out.println("5. Hapus data karyawan"); // New menu option
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    inputDataKaryawan(scanner, conn);
                    break;
                case 2:
                    editDataKaryawan(scanner, conn);
                    break;
                case 3:
                    laporanAbsensi(conn);
                    break;
                case 4:
                    System.out.print("Enter karyawan ID to view absensi report: ");
                    int karyawanId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    rekapAbsensi(conn, karyawanId); // Pass the karyawanId argument
                    break;
                case 5:
                    System.out.print("Enter karyawan ID to delete: ");
                    int deleteId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    deleteKaryawan(conn, deleteId); // Call deleteKaryawan method
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void userMenu(Scanner scanner, Connection conn, int loggedInUserId) {
        while (true) {
            System.out.println("\nUser Menu:");
            System.out.println("1. Presensi karyawan");
            System.out.println("2. Laporan absensi");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    presensiKaryawan(scanner, conn, loggedInUserId); // Pass loggedInUserId
                    break;
                case 2:
                    laporanAbsensi(conn);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void inputDataKaryawan(Scanner scanner, Connection conn) {
        try {
            System.out.print("Enter karyawan ID: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            System.out.print("Enter nama karyawan: ");
            String nama = scanner.nextLine().trim();
            System.out.print("Enter username karyawan: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password karyawan: ");
            String password = scanner.nextLine().trim();

            String sql = "INSERT INTO karyawan VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, nama);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, password);

            preparedStatement.executeUpdate();
            System.out.println("Data karyawan inserted successfully.");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    static void editDataKaryawan(Scanner scanner, Connection conn) {
    try {
        System.out.print("Enter karyawan ID to edit: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Periksa apakah karyawan dengan ID yang dimasukkan ada dalam database
        String checkSql = "SELECT * FROM karyawan WHERE id=?";
        PreparedStatement checkStatement = conn.prepareStatement(checkSql);
        checkStatement.setInt(1, id);
        ResultSet resultSet = checkStatement.executeQuery();

        if (!resultSet.next()) {
            System.out.println("No karyawan found with ID: " + id);
            return; // Keluar dari metode jika karyawan tidak ditemukan
        }

        // Jika karyawan ditemukan, minta input untuk data baru
        System.out.print("Enter new nama karyawan: ");
        String nama = scanner.nextLine().trim();
        System.out.print("Enter new username karyawan: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter new password karyawan: ");
        String password = scanner.nextLine().trim();

        // Jalankan perintah UPDATE
        String sql = "UPDATE karyawan SET nama=?, yourusername=?, yourpassword=? WHERE id=?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, nama);
        preparedStatement.setString(2, username);
        preparedStatement.setString(3, password);
        preparedStatement.setInt(4, id);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Data karyawan updated successfully.");
        } else {
            System.out.println("Failed to update data karyawan with ID: " + id);
        }
    } catch (SQLException se) {
        se.printStackTrace();
    }
}


    static void laporanAbsensi(Connection conn) {
    try {
        // Mendapatkan tanggal hari ini
        LocalDate today = LocalDate.now();
        // Konversi tanggal menjadi format yang sesuai untuk SQL
        String todayString = today.toString();

        // Query SQL untuk mengambil semua data karyawan dan status absensi pada tanggal hari ini
        String sql = "SELECT k.*, CASE WHEN a.karyawan_id IS NULL THEN 'Belum Absen' ELSE 'Sudah Absen' END AS status " +
                     "FROM karyawan k " +
                     "LEFT JOIN absensi a ON k.id = a.karyawan_id AND a.tanggal = ?";

        // Persiapkan pernyataan SQL
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, todayString); // Mengatur parameter tanggal

        // Eksekusi query dan dapatkan hasilnya
        ResultSet resultSet = preparedStatement.executeQuery();

        // Tampilkan hasil secara terstruktur
        System.out.println("Laporan Absensi pada tanggal " + todayString + ":");
        System.out.printf("%-5s %-20s %-20s %-20s %-20s%n", "ID", "Nama", "Username", "Password", "Status Absensi");
        System.out.println("---------------------------------------------------------------");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String nama = resultSet.getString("nama");
            String username = resultSet.getString("yourusername");
            String password = resultSet.getString("yourpassword");
            String statusAbsensi = resultSet.getString("status");
            
            if (!username.equals("admin")) {
        System.out.printf("%-5d %-20s %-20s %-20s %-20s%n", id, nama, username, password, statusAbsensi);
                 }
        }
    } catch (SQLException se) {
        se.printStackTrace();
    }
}



static void rekapAbsensi(Connection conn, int karyawanId) {
    try {
        // Query SQL untuk mengambil data absensi karyawan berdasarkan ID karyawan
        String sql = "SELECT a.tanggal, " +
                     "CASE WHEN a.tanggal IS NOT NULL THEN 'Sudah Absen' ELSE 'Belum Absen' END AS status " +
                     "FROM karyawan k LEFT JOIN absensi a ON k.id = a.karyawan_id " +
                     "WHERE k.id = ?";

        // Persiapkan pernyataan SQL
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, karyawanId);

        // Eksekusi query dan dapatkan hasilnya
        ResultSet resultSet = preparedStatement.executeQuery();

        // Tampilkan rekapan absensi karyawan
        System.out.println("Rekapan Absensi Karyawan (ID: " + karyawanId + "):");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-15s %-15s%n", "Tanggal", "Status");
        System.out.println("--------------------------------------------------");
        while (resultSet.next()) {
            String tanggal = resultSet.getString("tanggal");
            String status = resultSet.getString("status");
            System.out.printf("%-15s %-15s%n", tanggal, status);
        }
    } catch (SQLException se) {
        se.printStackTrace();
    }
}

static void deleteKaryawan(Connection conn, int karyawanId) {
    try {
        // Query SQL to delete karyawan data based on karyawan ID
        String sql = "DELETE FROM karyawan WHERE id = ?";

        // Prepare the SQL statement
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, karyawanId);

        // Execute the delete operation
        int rowsAffected = preparedStatement.executeUpdate();

        // Check if any rows were affected
        if (rowsAffected > 0) {
            System.out.println("Karyawan data with ID " + karyawanId + " has been deleted successfully.");
        } else {
            System.out.println("No karyawan data found with ID " + karyawanId + ". No deletion performed.");
        }
    } catch (SQLException se) {
        se.printStackTrace();
    }
}

static void presensiKaryawan(Scanner scanner, Connection conn, int loggedInUserId) {
    try {
        // Check if the user has already made a presensi for today
        String presensiCheckSql = "SELECT * FROM absensi WHERE karyawan_id = ? AND tanggal = ?";
        PreparedStatement presensiCheckStatement = conn.prepareStatement(presensiCheckSql);
        presensiCheckStatement.setInt(1, loggedInUserId);
        presensiCheckStatement.setString(2, LocalDate.now().toString());

        ResultSet resultSet = presensiCheckStatement.executeQuery();

        if (resultSet.next()) {
            System.out.println("Anda sudah melakukan presensi hari ini.");
        } else {
            // Insert presensi record for the current user for today
            String presensiInsertSql = "INSERT INTO absensi (karyawan_id, tanggal) VALUES (?, ?)";
            PreparedStatement presensiInsertStatement = conn.prepareStatement(presensiInsertSql);
            presensiInsertStatement.setInt(1, loggedInUserId);
            presensiInsertStatement.setString(2, LocalDate.now().toString());

            int rowsAffected = presensiInsertStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Presensi berhasil dicatat untuk hari ini.");
            } else {
                System.out.println("Gagal mencatat presensi.");
            }
        }
    } catch (SQLException se) {
        se.printStackTrace();
    }
}
}