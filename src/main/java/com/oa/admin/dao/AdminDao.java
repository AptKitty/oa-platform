package com.oa.admin.dao;

import com.oa.admin.entity.*;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminDao {
    // 会议室
    List<MeetingRoom> findAllRooms();
    MeetingRoom findRoomById(Long id);
    int insertRoom(MeetingRoom room);
    int updateRoom(MeetingRoom room);
    int deleteRoom(Long id);
    List<MeetingRoom> findAvailableRooms(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    int insertBooking(com.oa.admin.entity.RoomBooking booking);
    boolean isTimeSlotAvailable(@Param("roomId") Long roomId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    // 资产
    List<Asset> findAllAssets(@Param("keyword") String keyword);
    int insertAsset(Asset asset);
    int updateAsset(Asset asset);
    int insertAssetRecord(com.oa.admin.entity.AssetRecord record);
    int updateAssetStatus(@Param("id") Long id, @Param("status") String status);
    // 车辆
    List<Vehicle> findAllVehicles();
    int insertVehicle(Vehicle vehicle);
    int updateVehicle(Vehicle vehicle);
    int insertVehicleRecord(com.oa.admin.entity.VehicleRecord record);
}
