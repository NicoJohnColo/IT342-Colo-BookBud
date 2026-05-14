package edu.cit.colo.bookbud.features.wishlist.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.colo.bookbud.shared.dto.ApiResponse;
import edu.cit.colo.bookbud.features.wishlist.dto.AddToWishlistRequest;
import edu.cit.colo.bookbud.features.wishlist.dto.WishlistDTO;
import edu.cit.colo.bookbud.features.auth.security.JwtUtil;
import edu.cit.colo.bookbud.features.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
