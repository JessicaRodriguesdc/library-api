package com.library.libraryapi.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
public class BookTest {

    @Test
    @DisplayName("Deve relacionar uma lista de empréstimos a um livro.")
    public void should_loans_test() {
        // cenario
        Book book = mockBook();
        List<Loan> loans = new ArrayList<>();
        loans.add(mockLoan());

        // execucao
        book.withLoans(loans);

        // verificacao
        assertEquals(loans, book.getLoans());
    }

    private Book mockBook() {
        return Book.builder().isbn("123").author("Jessi").title("As aventuras").build();
    }

    private Loan mockLoan() {
        return Loan.builder().id(1l).customer("jessica").loanDate(LocalDate.now()).build();
    }
}
