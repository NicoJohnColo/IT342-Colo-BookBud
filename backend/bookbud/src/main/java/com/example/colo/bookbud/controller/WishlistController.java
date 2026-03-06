package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.wishlist.AddToWishlistRequest;
import com.example.colo.bookbud.dto.wishlist.WishlistDTO;
import com.example.colo.bookbud.security.JwtUtil;
import com.example.colo.bookbud.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistDTO>>> getMyWishlist(
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getMyWishlist(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistDTO>> addToWishlist(
            @Valid @RequestBody AddToWishlistRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(wishlistService.addToWishlist(userId, request)));
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable String wishlistId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        wishlistService.removeFromWishlist(wishlistId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
