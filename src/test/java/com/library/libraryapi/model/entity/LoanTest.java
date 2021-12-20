package com.library.libraryapi.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
public class LoanTest {

    @Test
    @DisplayName("Deve relacionar um livro ao empréstimo.")
    public void should_a_book_test() {
        //cenario
        Loan loan = mockLoan();

        //execucao
        loan.withBook(mockBook());

        //verificacao
        assertEquals(mockBook(), loan.getBook());
    }

    private Book mockBook() {
        return Book.builder().isbn("123").author("Jessi").title("As aventuras").build();
    }

    private Loan mockLoan(){
        return Loan.builder().id(1l).customer("jessica").loanDate(LocalDate.now()).build();
    }
}
