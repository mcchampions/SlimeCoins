package me.qscbm.plugins.slimecoins.data.impl;

import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.TransactionLog;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteProvider implements DataProvider {
    private final String filePath;
    private Connection connection;

    public SQLiteProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS slimecoins_balance (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "balance DECIMAL(20,2) NOT NULL DEFAULT 0.00, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS slimecoins_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "amount DECIMAL(20,2) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "source VARCHAR(36) NOT NULL, " +
                    "balance_after DECIMAL(20,2) NOT NULL, " +
                    "remark VARCHAR(255) DEFAULT '', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public BigDecimal getBalance(UUID uuid) {
        String sql = "SELECT balance FROM slimecoins_balance WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get balance", e);
        }
        return null;
    }

    @Override
    public void updateBalance(UUID uuid, String playerName, BigDecimal balance) {
        String sql = "UPDATE slimecoins_balance SET balance = ?, player_name = ?, updated_at = CURRENT_TIMESTAMP WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, balance);
            ps.setString(2, playerName);
            ps.setString(3, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update balance", e);
        }
    }

    @Override
    public boolean accountExists(UUID uuid) {
        String sql = "SELECT 1 FROM slimecoins_balance WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account", e);
        }
    }

    @Override
    public void createAccount(UUID uuid, String playerName) {
        String sql = "INSERT INTO slimecoins_balance (uuid, player_name, balance, updated_at) VALUES (?, ?, 0.00, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @Override
    public void logTransaction(UUID playerUuid, String playerName, BigDecimal amount,
                               String type, String source, BigDecimal balanceAfter, String remark) {
        String sql = "INSERT INTO slimecoins_log (player_uuid, player_name, amount, type, source, balance_after, remark, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, playerName);
            ps.setBigDecimal(3, amount);
            ps.setString(4, type);
            ps.setString(5, source);
            ps.setBigDecimal(6, balanceAfter);
            ps.setString(7, remark != null ? remark : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to log transaction", e);
        }
    }

    @Override
    public List<TransactionLog> getLogs(UUID uuid, int limit) {
        List<TransactionLog> logs = new ArrayList<>();
        String sql = "SELECT id, player_uuid, player_name, amount, type, source, balance_after, remark, created_at FROM slimecoins_log WHERE player_uuid = ? ORDER BY id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new TransactionLog(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            rs.getBigDecimal("amount"),
                            rs.getString("type"),
                            rs.getString("source"),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("remark"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get logs", e);
        }
        return logs;
    }

    @Override
    public List<BalanceRecord> getTopBalances(int limit, int offset) {
        List<BalanceRecord> records = new ArrayList<>();
        String sql = "SELECT uuid, player_name, balance, updated_at FROM slimecoins_balance ORDER BY balance DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new BalanceRecord(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name"),
                            rs.getBigDecimal("balance"),
                            rs.getTimestamp("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get top balances", e);
        }
        return records;
    }
}
