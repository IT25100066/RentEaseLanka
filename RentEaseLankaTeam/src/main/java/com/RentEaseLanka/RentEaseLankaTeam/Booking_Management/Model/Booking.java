package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
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
@Table (name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BookingId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private Customer customer;

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

    // Constructors
    public Booking() {
        LocalDateTime createdAt = LocalDateTime.now();
    }

    private void makeConfirmed(){
        this.status = status.transitionTo(BookingStatus.CONFIRMED);
        log.info("Booking is confirmed");
    }
    private void makeCancelled(){
        this.status = status.transitionTo(BookingStatus.CANCELLED);
        log.info("Booking is Cancelled");

    }
    private void makeActive(){
        this.status = status.transitionTo(BookingStatus.ACTIVE);
        log.info("Booking is Active");
    }


}

