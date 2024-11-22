package com.example.restapi.controller;

import com.example.restapi.service.BookService;
import com.example.restapi.model.Book;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@Data
@Controller
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
    // Find books by author
    @GetMapping("/author/{author}")
    public List<Book> getBooksByAuthor(@PathVariable String author) {
        return bookService.getBooksByAuthor(author);
    }

    // Find books by title (partial match)
    @GetMapping("/title/{title}")
    public List<Book> getBooksByTitle(@PathVariable String title) {
        return bookService.getBooksByTitle(title);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            // Return a success message with the updated book info
            return ResponseEntity.ok("Book updated successfully. Updated book: " + updatedBook.getTitle());
        } catch (RuntimeException e) {
            // If book is not found, return a 404 error with the appropriate message
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportBooksToCSV() {
        try {
            byte[] data = bookService.exportBooksToCSV();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"books.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(data);
        } catch (IOException e) {
            // Handle the error (e.g., return 500 Internal Server Error)
            return ResponseEntity.status(500).body("Error exporting CSV".getBytes());
        }
    }
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportBooksToExcel() {
        try {
            byte[] data = bookService.exportBooksToExcel();

            // Return the Excel file as a response with appropriate headers for download
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"books.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (IOException e) {
            // Handle any exceptions (e.g., IO errors during file generation)
            return ResponseEntity.status(500).body("Error exporting Excel".getBytes());
        }
    }


}
