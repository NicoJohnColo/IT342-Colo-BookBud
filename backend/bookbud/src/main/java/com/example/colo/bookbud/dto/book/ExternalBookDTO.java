package com.example.colo.bookbud.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBookDTO {
    private String title;
    private String author;
    private String genre;
    private String coverImageUrl;
}
