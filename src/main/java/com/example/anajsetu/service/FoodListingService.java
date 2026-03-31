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

    public FoodListing createListing(FoodListing foodListing) {
        foodListing.setStatus("AVAILABLE");
        return foodListingRepository.save(foodListing);
    }

    public List<FoodListing> getAvailableListings() {
        return foodListingRepository.findByStatus("AVAILABLE");
    }

    public Optional<FoodListing> getListingById(int id) {
        return foodListingRepository.findById(id);
    }

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
        listing.setClaimedBy(ngo); 

        try {
            return foodListingRepository.save(listing);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Conflict: Already claimed by another user.");
        }
    }

    public void deleteListing(int id) {
        foodListingRepository.deleteById(id);
    }
}