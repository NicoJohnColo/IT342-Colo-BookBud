package edu.cit.colo.bookbud.features.books.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.colo.bookbud.features.books.dto.BookDTO;
import edu.cit.colo.bookbud.features.books.dto.CreateBookRequest;
import edu.cit.colo.bookbud.features.books.dto.ExternalBookDTO;
import edu.cit.colo.bookbud.features.books.dto.UpdateBookRequest;
import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.transactions.entity.Transaction;
import edu.cit.colo.bookbud.features.transactions.repository.TransactionRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.features.wishlist.repository.WishlistRepository;
import edu.cit.colo.bookbud.shared.dto.PaginatedResponse;
import edu.cit.colo.bookbud.shared.exception.BusinessException;
import edu.cit.colo.bookbud.shared.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WishlistRepository wishlistRepository;

    @Value("${app.upload.book-images-dir:uploads/book-images}")
    private String bookImagesDir;

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Transactional(readOnly = true)
    public PaginatedResponse<BookDTO> getAllBooks(String q, String genre, String condition,
                                                   String type, BigDecimal minPrice, BigDecimal maxPrice,
                                                   String status, int page, int size) {
        Book.Status bookStatus = status != null ? parseEnum(Book.Status.class, status, "status") : Book.Status.Available;
        Book.Condition bookCondition = condition != null ? parseEnum(Book.Condition.class, condition, "condition") : null;
        Book.TransactionType transactionType = type != null ? parseEnum(Book.TransactionType.class, type, "type") : null;

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern)
                ));
            }
            if (genre != null) predicates.add(cb.equal(root.get("genre"), genre));
            if (bookCondition != null) predicates.add(cb.equal(root.get("condition"), bookCondition));
            if (transactionType != null) predicates.add(cb.equal(root.get("transactionType"), transactionType));
            if (bookStatus != null) predicates.add(cb.equal(root.get("status"), bookStatus));
            if (minPrice != null) predicates.add(cb.or(
                cb.greaterThanOrEqualTo(root.get("priceRent"), minPrice),
                cb.greaterThanOrEqualTo(root.get("priceSale"), minPrice)
            ));
            if (maxPrice != null) predicates.add(cb.or(
                cb.lessThanOrEqualTo(root.get("priceRent"), maxPrice),
                cb.lessThanOrEqualTo(root.get("priceSale"), maxPrice)
            ));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAll(spec, pageable);

        return PaginatedResponse.<BookDTO>builder()
                .content(books.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()))
                .page(books.getNumber())
                .size(books.getSize())
                .totalElements(books.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public BookDTO getBookById(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));
        return mapToDTO(book);
    }

    @Transactional
    public BookDTO createBook(String ownerId, CreateBookRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
            .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .condition(parseEnum(Book.Condition.class, request.getCondition(), "condition"))
                .transactionType(parseEnum(Book.TransactionType.class, request.getTransactionType(), "transactionType"))
                .priceRent(request.getPriceRent())
                .priceSale(request.getPriceSale())
                .status(Book.Status.Available)
                .owner(owner)
                .build();

        book = bookRepository.save(book);
        return mapToDTO(book);
    }

    @Transactional
    public BookDTO updateBook(String bookId, String requestingUserId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        boolean isOwner = book.getOwner().getUserId().equals(requestingUserId);
        boolean isAdmin = userRepository.findById(requestingUserId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("AUTH-003", "Not owner or admin");
        }

        // Check if book is locked by active transaction
        List<Transaction> activeTransactions = transactionRepository.findByBookBookId(bookId).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Active || t.getStatus() == Transaction.Status.Pending)
                .collect(Collectors.toList());

        if (!activeTransactions.isEmpty()) {
            throw new BusinessException("BUSINESS-001", "Book status is locked by an active transaction");
        }

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getGenre() != null) book.setGenre(request.getGenre());
        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            book.setDescription(description.isEmpty() ? null : description);
        }
        if (request.getCondition() != null && !request.getCondition().isBlank()) {
            book.setCondition(parseEnum(Book.Condition.class, request.getCondition(), "condition"));
        }
        if (request.getTransactionType() != null && !request.getTransactionType().isBlank()) {
            book.setTransactionType(parseEnum(Book.TransactionType.class, request.getTransactionType(), "transactionType"));
        }
        if (request.getPriceRent() != null) book.setPriceRent(request.getPriceRent());
        if (request.getPriceSale() != null) book.setPriceSale(request.getPriceSale());

        book = bookRepository.save(book);
        return mapToDTO(book);
    }

    @Transactional
    public void deleteBook(String bookId, String requestingUserId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        boolean isOwner = book.getOwner().getUserId().equals(requestingUserId);
        boolean isAdmin = userRepository.findById(requestingUserId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("AUTH-003", "Insufficient permissions");
        }

        List<Transaction> activeTransactions = transactionRepository.findByBookBookId(bookId).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Active || t.getStatus() == Transaction.Status.Pending)
                .collect(Collectors.toList());

        if (!activeTransactions.isEmpty()) {
            throw new BusinessException("BUSINESS-001", "Book status is locked by an active transaction");
        }

        if (isAdmin && !isOwner) {
            // Admin deletion - set status to Unavailable
            book.setStatus(Book.Status.Unavailable);
            bookRepository.save(book);
            // TODO: Send notification to owner
        } else {
            // Owner deletion - cascade delete wishlist entries
            deleteImageQuietly(book.getImageFileName());
            wishlistRepository.deleteByBookBookId(bookId);
            bookRepository.delete(book);
        }
    }

    @Transactional
    public BookDTO uploadBookImage(String bookId, String requestingUserId, MultipartFile image) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        boolean isOwner = book.getOwner().getUserId().equals(requestingUserId);
        boolean isAdmin = userRepository.findById(requestingUserId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("AUTH-003", "Not owner or admin");
        }

        validateImage(image);

        String oldImageFileName = book.getImageFileName();
        String extension = resolveFileExtension(image);
        String storedFileName = bookId + "_" + System.currentTimeMillis() + extension;
        Path storagePath = resolveImagePath(storedFileName);

        try {
            Files.createDirectories(storagePath.getParent());
            Files.copy(image.getInputStream(), storagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("FILE-001", "Failed to store image");
        }

        book.setImageFileName(storedFileName);
        book = bookRepository.save(book);

        if (oldImageFileName != null && !oldImageFileName.isBlank() && !oldImageFileName.equals(storedFileName)) {
            deleteImageQuietly(oldImageFileName);
        }

        return mapToDTO(book);
    }

    @Transactional(readOnly = true)
    public BookImageFile getBookImage(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        if (book.getImageFileName() == null || book.getImageFileName().isBlank()) {
            throw new ResourceNotFoundException("DB-001", "Book image not found");
        }

        Path imagePath = resolveImagePath(book.getImageFileName());
        if (!Files.exists(imagePath)) {
            throw new ResourceNotFoundException("DB-001", "Book image file not found");
        }

        try {
            byte[] content = Files.readAllBytes(imagePath);
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            return new BookImageFile(content, contentType);
        } catch (IOException ex) {
            throw new BusinessException("FILE-001", "Failed to read image");
        }
    }

    @Transactional(readOnly = true)
    public List<ExternalBookDTO> searchExternalBooks(String query) {
        try {
            // Google Books API endpoint
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&maxResults=5";
            
            // Make HTTP request to Google Books API
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "BookBud/1.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Read response with try-with-resources
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    // Parse JSON response
                    return parseGoogleBooksResponse(response.toString());
                }
            } else {
                return List.of();
            }
        } catch (Exception e) {
            return List.of();
        }
    }
    
    private List<ExternalBookDTO> parseGoogleBooksResponse(String jsonResponse) {
        List<ExternalBookDTO> books = new ArrayList<>();
        try {
            // Simple JSON parsing (you might want to use a proper JSON library)
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonResponse);
            
            if (root.has("items")) {
                for (com.fasterxml.jackson.databind.JsonNode item : root.get("items")) {
                    com.fasterxml.jackson.databind.JsonNode volumeInfo = item.get("volumeInfo");
                    
                    String title = volumeInfo.has("title") ? volumeInfo.get("title").asText() : "";
                    List<String> authors = new ArrayList<>();
                    if (volumeInfo.has("authors")) {
                        for (com.fasterxml.jackson.databind.JsonNode author : volumeInfo.get("authors")) {
                            authors.add(author.asText());
                        }
                    }
                    String description = volumeInfo.has("description") ? volumeInfo.get("description").asText() : "";
                    List<String> categories = new ArrayList<>();
                    if (volumeInfo.has("categories")) {
                        for (com.fasterxml.jackson.databind.JsonNode category : volumeInfo.get("categories")) {
                            categories.add(category.asText());
                        }
                    }
                    
                    ExternalBookDTO book = ExternalBookDTO.builder()
                        .title(title)
                        .authors(authors)
                        .description(description)
                        .categories(categories)
                        .build();
                    
                    books.add(book);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Google Books response: " + e.getMessage());
        }
        return books;
    }

    private BookDTO mapToDTO(Book book) {
        return BookDTO.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .description(book.getDescription())
                .imageUrl(buildImageUrl(book))
                .condition(book.getCondition() != null ? book.getCondition().name() : null)
                .priceRent(book.getPriceRent())
                .priceSale(book.getPriceSale())
                .transactionType(book.getTransactionType() != null ? book.getTransactionType().name() : null)
                .status(book.getStatus() != null ? book.getStatus().name() : null)
                .ownerId(book.getOwner() != null ? book.getOwner().getUserId() : null)
                .createdAt(book.getCreatedAt() != null ? book.getCreatedAt().toString() : null)
                .build();
    }

    private String buildImageUrl(Book book) {
        if (book.getImageFileName() == null || book.getImageFileName().isBlank() || book.getBookId() == null) {
            return null;
        }
        return "/api/v1/books/" + book.getBookId() + "/image";
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("VALID-001", "Image file is required");
        }

        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException("VALID-001", "Image exceeds maximum size of 5MB");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("VALID-001", "Unsupported image format. Allowed: JPG, PNG, WEBP, GIF");
        }
    }

    private String resolveFileExtension(MultipartFile image) {
        String contentType = image.getContentType();
        if ("image/jpeg".equals(contentType)) return ".jpg";
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        if ("image/gif".equals(contentType)) return ".gif";
        throw new BusinessException("VALID-001", "Unsupported image format");
    }

    private Path resolveImagePath(String fileName) {
        return Paths.get(bookImagesDir).toAbsolutePath().normalize().resolve(fileName);
    }

    private void deleteImageQuietly(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolveImagePath(fileName));
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().replace('-', '_').replace(' ', '_');
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(normalized)) {
                return constant;
            }
        }

        throw new BusinessException("VALID-001", "Invalid " + fieldName + ": " + rawValue);
    }

    public record BookImageFile(byte[] content, String contentType) {}
}
