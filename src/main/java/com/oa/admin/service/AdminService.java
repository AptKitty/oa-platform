package com.oa.admin.service;

import com.oa.admin.dao.AdminDao;
import com.oa.admin.entity.*;
import com.oa.common.MyBatisUtil;
import com.oa.common.BusinessException;
import java.time.LocalDateTime;
import java.util.List;

public class AdminService {

    private AdminDao getDao() { return MyBatisUtil.openSession().getMapper(AdminDao.class); }

    public List<MeetingRoom> getAllRooms() { return getDao().findAllRooms(); }
    public void addRoom(MeetingRoom room) { getDao().insertRoom(room); }
    public void updateRoom(MeetingRoom room) { getDao().updateRoom(room); }
    public void deleteRoom(Long id) { getDao().deleteRoom(id); }
    public List<MeetingRoom> getAvailableRooms(LocalDateTime start, LocalDateTime end) {
        return getDao().findAvailableRooms(start, end);
    }
    public void bookRoom(RoomBooking booking) {
        AdminDao dao = getDao();
        if (!dao.isTimeSlotAvailable(booking.getRoomId(), booking.getStartTime(), booking.getEndTime())) {
            throw new BusinessException("该时段已被预约");
        }
        dao.insertBooking(booking);
    }

    public List<Asset> getAllAssets(String keyword) { return getDao().findAllAssets(keyword); }
    public void addAsset(Asset asset) { getDao().insertAsset(asset); }
    public void updateAsset(Asset asset) { getDao().updateAsset(asset); }
    public void borrowAsset(AssetRecord record) { getDao().insertAssetRecord(record); }
    public void returnAsset(AssetRecord record) { getDao().insertAssetRecord(record); }

    public List<Vehicle> getAllVehicles() { return getDao().findAllVehicles(); }
    public void addVehicle(Vehicle vehicle) { getDao().insertVehicle(vehicle); }
    public void updateVehicle(Vehicle vehicle) { getDao().updateVehicle(vehicle); }
    public void addVehicleRecord(VehicleRecord record) { getDao().insertVehicleRecord(record); }
}