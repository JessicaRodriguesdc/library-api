package com.library.libraryapi.api.resource;

import com.library.libraryapi.api.dto.BooKDTO;
import com.library.libraryapi.api.dto.LoanDto;
import com.library.libraryapi.api.dto.LoanFilterDTO;
import com.library.libraryapi.api.dto.ReturnedLoanDTO;
import com.library.libraryapi.model.entity.Book;
import com.library.libraryapi.model.entity.Loan;
import com.library.libraryapi.service.BookService;
import com.library.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Api("Loan API")
@Slf4j
public class LoanController {

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a loan")
    public Long create(@RequestBody LoanDto dto){

        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(()->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);
        return entity.getId();

    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Updates a loan")
    public void returnBook (
            @PathVariable Long id,
            @RequestBody ReturnedLoanDTO dto){
        Loan loan = service.getById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());

        service.update(loan);
    }

    @GetMapping
    @ApiOperation("Find loans by params")
    public Page<LoanDto> find (LoanFilterDTO dto, Pageable pageaRequest){
        Page<Loan> result = service.find(dto, pageaRequest);
        List<LoanDto> loans = result
                .getContent()
                .stream()
                .map(entity -> {

                    Book book = entity.getBook();
                    BooKDTO booKDTO = modelMapper.map(book, BooKDTO.class);
                    LoanDto loanDTO = modelMapper.map(entity, LoanDto.class);
                    loanDTO.setBook(booKDTO);
                    return loanDTO;

                }).collect(Collectors.toList());
        return new PageImpl<LoanDto>(loans,pageaRequest,result.getTotalElements());
    }
}
