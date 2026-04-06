package com.example.anajsetu.service;

import com.example.anajsetu.model.FoodListing;
import com.example.anajsetu.model.User;
import com.example.anajsetu.repository.FoodListingRepository;
import com.example.anajsetu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FoodListingService {

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public FoodListing createListing(FoodListing foodListing) {
        foodListing.setStatus("AVAILABLE");
        return foodListingRepository.save(foodListing);
    }

    // GET ALL AVAILABLE
    public List<FoodListing> getAvailableListings() {
        return foodListingRepository.findByStatus("AVAILABLE");
    }

    // GET BY ID
    public Optional<FoodListing> getListingById(int id) {
        return foodListingRepository.findById(id);
    }

    // GET BY DONOR ID
    public List<FoodListing> getByDonor(int donorId) {
        return foodListingRepository.findByDonorId(donorId);
    }

    // GET BY NGO ID
    public List<FoodListing> getByNgo(int ngoId) {
        return foodListingRepository.findByNgoId(ngoId);
    }

    // GET BY DRIVER ID
    public List<FoodListing> getByDriver(int driverId) {
        return foodListingRepository.findByDriverId(driverId);
    }

    // CLAIM
    @Transactional
    public FoodListing claimListing(int listingId, int ngoId) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        User ngo = userRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found"));

        if (!"AVAILABLE".equalsIgnoreCase(listing.getStatus())) {
            throw new RuntimeException("Listing no longer available.");
        }

        listing.setStatus("CLAIMED");
        listing.setNgoId(ngoId);                        // ✅ set ngoId explicitly
        listing.setClaimedBy(ngo);
       listing.setClaimedByName(ngo.getName() != null ? ngo.getName() : "");
        listing.setNgoPhone(ngo.getPhone());

        // ✅ AUTO-ASSIGN: find any available delivery partner
        List<User> drivers = userRepository.findByRole("DELIVERY");
        if (!drivers.isEmpty()) {
            listing.setDriverId(drivers.get(0).getId());
            log.info("Auto-assigned driver: {} (id={})", drivers.get(0).getName(), drivers.get(0).getId());
        } else {
            log.warn("No delivery partner found — driver_id will be null for listing {}", listingId);
        }

        try {
            return foodListingRepository.save(listing);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Conflict: Already claimed by another user.");
        }
    }

    // DELIVER (after OTP verified)
    @Transactional
    public FoodListing deliverListing(int listingId) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!"CLAIMED".equalsIgnoreCase(listing.getStatus())) {
            throw new RuntimeException("Listing must be CLAIMED before marking as DELIVERED.");
        }

        listing.setStatus("DELIVERED");

        try {
            return foodListingRepository.save(listing);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Conflict: Delivery status already updated.");
        }
    }

    // DELETE
    public void deleteListing(int id) {
        foodListingRepository.deleteById(id);
    }
}