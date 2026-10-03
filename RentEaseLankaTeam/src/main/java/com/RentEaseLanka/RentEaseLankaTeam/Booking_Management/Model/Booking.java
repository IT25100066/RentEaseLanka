<<<<<<< HEAD
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(nullable = false)
    private LocalDateTime returnDateTime;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String returnLocation;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private boolean withDriver;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updateAt;

    @Column
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    protected static double driverFee;

    public static void calculateDriverFee(long days) {
        driverFee = 3000 * days;
    }

    public static double getDriverFee() {
        return driverFee;
    }

    public void setDriverFee(double driverFee) {
        Booking.driverFee = driverFee;
    }

    public Booking() {
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = BookingStatus.PENDING;
            this.createdAt = LocalDateTime.now();
            this.updateAt = LocalDateTime.now();
        }
    }

    public void makePending() {
        this.status = status.transitionTo(BookingStatus.PENDING);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Pending");
    }

    public void makeConfirmed() {
        this.status = status.transitionTo(BookingStatus.CONFIRMED);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is confirmed");
    }

    public void makeCancelled() {
        this.status = status.transitionTo(BookingStatus.CANCELLED);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Cancelled");

    }

    public void makeActive() {
        this.status = status.transitionTo(BookingStatus.ACTIVE);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Active");
    }

    public void makeCompleted() {
        this.status = status.transitionTo(BookingStatus.COMPLETED);
        this.completedAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Completed");
    }

}
=======
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(nullable = false)
    private LocalDateTime returnDateTime;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String returnLocation;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private boolean withDriver;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updateAt;

    @Column
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    protected static double driverFee;

    public static void calculateDriverFee(long days) {
        driverFee = 3000 * days;
    }

    public static double getDriverFee() {
        return driverFee;
    }

    public void setDriverFee(double driverFee) {
        Booking.driverFee = driverFee;
    }

    public Booking() {
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = BookingStatus.PENDING;
            this.createdAt = LocalDateTime.now();
            this.updateAt = LocalDateTime.now();
        }
    }

    public void makePending() {
        this.status = status.transitionTo(BookingStatus.PENDING);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Pending");
    }

    public void makeConfirmed() {
        this.status = status.transitionTo(BookingStatus.CONFIRMED);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is confirmed");
    }

    public void makeCancelled() {
        this.status = status.transitionTo(BookingStatus.CANCELLED);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Cancelled");

    }

    public void makeActive() {
        this.status = status.transitionTo(BookingStatus.ACTIVE);
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Active");
    }

    public void makeCompleted() {
        this.status = status.transitionTo(BookingStatus.COMPLETED);
        this.completedAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        log.info("Booking is Completed");
    }

}
>>>>>>> 21bee750d2eb336b0bafde9dc94de064033fd540
