package com.example.restapi.service;
import com.example.restapi.model.Book;
import com.example.restapi.repository.BookRepository;
import com.opencsv.CSVWriter;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;

@Service
@Data
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBookById(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
        }
        else{
            throw new RuntimeException("Book not found with id: " + id);
        }
    }
    public Book updateBook(Long id, Book book) {
        if (bookRepository.existsById(id)) {
            // Retrieve the existing book from the repository
            Book existingBook = bookRepository.findById(id).get();

            // Update the fields of the existing book with the new values
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            // Add other fields if necessary

            // Save the updated book
            return bookRepository.save(existingBook);
        } else {
            throw new RuntimeException("Book not found with id: " + id);
        }
    }

    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    public byte[] exportBooksToCSV() throws IOException {
        // Fetch the list of books
        List<Book> books = bookRepository.findAll();

        // Set up CSVWriter with ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(byteArrayOutputStream));

        // Write CSV Header
        csvWriter.writeNext(new String[]{"ID", "Title", "Author", "Publisher", "Year"});

        // Write book data rows
        for (Book book : books) {
            csvWriter.writeNext(new String[]{
                    book.getId().toString(),
                    book.getTitle(),
                    book.getAuthor(),
            });
        }

        // Close the writer
        csvWriter.close();

        // Return the CSV data as a byte array
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] exportBooksToExcel() throws IOException {
        List<Book> books = bookRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Books");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Title");
        headerRow.createCell(2).setCellValue("Author");
        headerRow.createCell(3).setCellValue("Publisher");
        headerRow.createCell(4).setCellValue("Year");

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Add book data rows
        int rowNum = 1;
        for (Book book : books) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(book.getId());
            row.createCell(1).setCellValue(book.getTitle());
            row.createCell(2).setCellValue(book.getAuthor());
        }

        // Create a bold font style for the header row
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        // Set background color for header (light gray)
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // Apply style to each cell in the header row
        for (Cell cell : headerRow) {
            cell.setCellStyle(style);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        return byteArrayOutputStream.toByteArray();
    }
}
