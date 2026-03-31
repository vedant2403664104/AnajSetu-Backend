package com.example.anajsetu.controller;

import com.example.anajsetu.model.FoodListing;
import com.example.anajsetu.service.FoodListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "http://localhost:3000")
public class FoodListingController {

    @Autowired
    private FoodListingService foodListingService;

    @PostMapping
    public ResponseEntity<FoodListing> createListing(@RequestBody FoodListing foodListing) {
        return new ResponseEntity<>(foodListingService.createListing(foodListing), HttpStatus.CREATED);
    }

    @GetMapping("/available")
    public ResponseEntity<List<FoodListing>> getAvailable() {
        return new ResponseEntity<>(foodListingService.getAvailableListings(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodListing> getListingById(@PathVariable int id) {
        return foodListingService.getListingById(id)
                .map(listing -> new ResponseEntity<>(listing, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}/claim/{ngoId}")
    public ResponseEntity<?> claimListing(@PathVariable int id, @PathVariable int ngoId) {
        try {
            FoodListing updated = foodListingService.claimListing(id, ngoId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable int id) {
        foodListingService.deleteListing(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}