package com.example.anajsetu.controller;

import com.example.anajsetu.model.FoodListing;
import com.example.anajsetu.service.FoodListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-listings")
@CrossOrigin(origins = "http://localhost:3000")
public class FoodListingController {

    @Autowired
    private FoodListingService foodListingService;

    // CREATE listing
    @PostMapping
    public ResponseEntity<FoodListing> createListing(@RequestBody FoodListing foodListing) {
        return new ResponseEntity<>(foodListingService.createListing(foodListing), HttpStatus.CREATED);
    }

    // GET all available
    @GetMapping("/available")
    public ResponseEntity<List<FoodListing>> getAvailable() {
        return new ResponseEntity<>(foodListingService.getAvailableListings(), HttpStatus.OK);
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodListing> getListingById(@PathVariable int id) {
        return foodListingService.getListingById(id)
                .map(listing -> new ResponseEntity<>(listing, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // GET by Donor ID
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<List<FoodListing>> getByDonor(@PathVariable int donorId) {
        return new ResponseEntity<>(foodListingService.getByDonor(donorId), HttpStatus.OK);
    }

    // GET by NGO ID
    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<List<FoodListing>> getByNgo(@PathVariable int ngoId) {
        return new ResponseEntity<>(foodListingService.getByNgo(ngoId), HttpStatus.OK);
    }

    // GET by Driver ID
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<FoodListing>> getByDriver(@PathVariable int driverId) {
        return new ResponseEntity<>(foodListingService.getByDriver(driverId), HttpStatus.OK);
    }

    // CLAIM listing
    @PutMapping("/{id}/claim/{ngoId}")
    public ResponseEntity<?> claimListing(@PathVariable int id, @PathVariable int ngoId) {
        try {
            FoodListing updated = foodListingService.claimListing(id, ngoId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // DELIVER listing (after OTP verified)
    @PutMapping("/{id}/deliver")
    public ResponseEntity<?> deliverListing(@PathVariable int id) {
        try {
            FoodListing updated = foodListingService.deliverListing(id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // DELETE listing
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable int id) {
        foodListingService.deleteListing(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}