package edu.cit.colo.bookbud.dto.book;

import java.util.List;

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
    private List<String> authors;
    private String description;
    private List<String> categories;
    private String coverImageUrl;
}
