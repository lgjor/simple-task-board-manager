package org.desviante.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Book extends Card {

    private String author;
    private Integer pages;
    private Integer actualPage;
    private Integer year;
    private String publisher;
    private String isbn;
    private String genre;
    private String language;
    private String coverUrl;
}
