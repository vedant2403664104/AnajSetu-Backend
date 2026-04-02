package com.example.anajsetu.repository;

import com.example.anajsetu.model.FoodListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Integer> {

    List<FoodListing> findByStatus(String status);

    List<FoodListing> findByDonorId(int donorId);

    List<FoodListing> findByNgoId(int ngoId);

    List<FoodListing> findByDriverId(int driverId);

    List<FoodListing> findByFoodType(String foodType);

    List<FoodListing> findByStatusAndFoodType(String status, String foodType);
}