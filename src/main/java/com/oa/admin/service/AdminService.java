package com.oa.admin.service;

import com.oa.admin.dao.AdminDao;
import com.oa.admin.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.BusinessException;
import org.apache.ibatis.session.SqlSession;
import java.time.LocalDateTime;
import java.util.List;

public class AdminService {

    public List<MeetingRoom> getAllRooms() {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findAllRooms(); }
    }

    public void addRoom(MeetingRoom room) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertRoom(room); }
    }

    public void updateRoom(MeetingRoom room) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).updateRoom(room); }
    }

    public void deleteRoom(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).deleteRoom(id); }
    }

    public List<MeetingRoom> getAvailableRooms(LocalDateTime start, LocalDateTime end) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            return s.getMapper(AdminDao.class).findAvailableRooms(start, end);
        }
    }

    public void bookRoom(RoomBooking booking) {
        try (SqlSession s = MyBatisUtil.openSession()) {
            AdminDao dao = s.getMapper(AdminDao.class);
            if (!dao.isTimeSlotAvailable(booking.getRoomId(), booking.getStartTime(), booking.getEndTime())) {
                throw new BusinessException("该时段已被预约");
            }
            dao.insertBooking(booking);
        }
    }

    public List<Asset> getAllAssets(String keyword) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findAllAssets(keyword); }
    }

    public void addAsset(Asset asset) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertAsset(asset); }
    }

    public void updateAsset(Asset asset) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).updateAsset(asset); }
    }

    public void borrowAsset(AssetRecord record) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertAssetRecord(record); }
    }

    public void returnAsset(AssetRecord record) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertAssetRecord(record); }
    }

    public List<Vehicle> getAllVehicles() {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findAllVehicles(); }
    }

    public void addVehicle(Vehicle vehicle) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertVehicle(vehicle); }
    }

    public void updateVehicle(Vehicle vehicle) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).updateVehicle(vehicle); }
    }

    public void addVehicleRecord(VehicleRecord record) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).insertVehicleRecord(record); }
    }

    public void updateVehicleStatus(Long id, String status) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).updateVehicleStatus(id, status); }
    }

    public List<VehicleRecord> getVehicleRecords(Long vehicleId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findVehicleRecords(vehicleId); }
    }

    public void scrapAsset(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).updateAssetStatus(id, "SCRAPPED"); }
    }

    public List<AssetRecord> getAssetRecords(Long assetId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findAssetRecordsByAssetId(assetId); }
    }

    public List<RoomBooking> getMyBookings(Long userId) {
        try (SqlSession s = MyBatisUtil.openSession()) { return s.getMapper(AdminDao.class).findBookingsByUser(userId); }
    }

    public void cancelBooking(Long id) {
        try (SqlSession s = MyBatisUtil.openSession()) { s.getMapper(AdminDao.class).cancelBooking(id); }
    }
}