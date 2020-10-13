package com.library.libraryapi.api.resource;

import com.library.libraryapi.api.dto.BooKDTO;
import com.library.libraryapi.api.dto.LoanDto;
import com.library.libraryapi.model.entity.Book;
import com.library.libraryapi.model.entity.Loan;
import com.library.libraryapi.service.BookService;
import com.library.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a book")
    public BooKDTO create(@RequestBody @Valid BooKDTO dto){
        log.info(" creating a book for isbn: {} ",dto.getIsbn());
        Book entity = modelMapper.map(dto,Book.class);
        entity = service.save(entity);
        return modelMapper.map(entity,BooKDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtains a book details by id")
    public BooKDTO get(@PathVariable Long id){
        log.info(" Obtaining datails for book id: {} ",id);
        return service
                .getById(id)
                .map( book -> modelMapper.map(book,BooKDTO.class))
                .orElseThrow( () ->  new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ApiOperation("Deletes a book by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(code= 204, message = "Book succesfully deleted")
    })
    public void delete(@PathVariable Long id){
        log.info(" Deleting a book by id: {} ",id);
        Book book = service.getById(id).orElseThrow( () ->  new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Updates a book")
    public BooKDTO update( @PathVariable Long id,@RequestBody @Valid BooKDTO dto ){
        log.info(" Updating a book by id: {} ",id);
        return service.getById(id).map( book -> {

            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book = service.update(book);
            return modelMapper.map(book, BooKDTO.class);

        }).orElseThrow( () ->  new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Find books by params")
    public Page<BooKDTO> find(BooKDTO dto, Pageable pageableRequest){
        Book filter = modelMapper.map(dto,Book.class);
        Page<Book> result = service.find(filter, pageableRequest);
        List<BooKDTO> list = result.getContent().stream()
                        .map( entity -> modelMapper.map(entity,BooKDTO.class) )
                        .collect(Collectors.toList());

        return new PageImpl<BooKDTO>(list,pageableRequest,result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Obtains a book loans")
    public Page<LoanDto> loansByBook(@PathVariable Long id, Pageable pageable){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDto> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BooKDTO bookDTO = modelMapper.map(loanBook, BooKDTO.class);
                    LoanDto loanDto = modelMapper.map(loan, LoanDto.class);
                    loanDto.setBook(bookDTO);
                    return loanDto;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDto>(list, pageable, result.getTotalElements());
    }
}
