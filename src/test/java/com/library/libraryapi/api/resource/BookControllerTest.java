package com.library.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.libraryapi.api.dto.BooKDTO;
import com.library.libraryapi.exception.BusinessException;
import com.library.libraryapi.model.entity.Book;
import com.library.libraryapi.service.BookService;
import com.library.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.Optional;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

        static String BOOK_API = "/api/books";

        @Autowired
        MockMvc mvc;

        // Criar uma instancia mocada
        @MockBean
        BookService service;

        @MockBean
        LoanService loanService;

        // validacao de integridade
        @Test
        @DisplayName("Deve criar um livro com sucesso.")
        public void create_book_rest_test() throws Exception {
                BooKDTO dto = createNewBook();
                Book saveBook = Book.builder().id(10l).author("Jessi").title("As aventuras").isbn("001").build();

                BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(saveBook);

                String json = new ObjectMapper().writeValueAsString(dto);

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc.perform(request).andExpect(status().isCreated()).andExpect(jsonPath("id").isNotEmpty())
                                .andExpect(jsonPath("id").value(10l)).andExpect(jsonPath("title").value(dto.getTitle()))
                                .andExpect(jsonPath("author").value(dto.getAuthor()))
                                .andExpect(jsonPath("isbn").value(dto.getIsbn()))

                ;
        }

        @Test
        @DisplayName("Deve  lancar erro de validacao quando nao houver dados suficiente para criacao do livro.")
        public void create_invalid_book_rest_test() throws Exception {
                String json = new ObjectMapper().writeValueAsString(new BooKDTO());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", hasSize(3)));
        }

        // validacao de regra de negocio
        @Test
        @DisplayName("Deve lancar um erro ao tentar cadastrar um livro com isbn ja utilizado.")
        public void create_book_with_duplicated_isbn_rest_test() throws Exception {
                BooKDTO dto = createNewBook();
                String json = new ObjectMapper().writeValueAsString(dto);
                String mensagemErro = "Isbn ja cadastrado.";
                BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                .content(json);

                mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", hasSize(1)))
                                .andExpect(jsonPath("errors[0]").value(mensagemErro));

        }

        @Test
        @DisplayName("Deve obter informacoes de um livro")
        public void get_book_details_rest_test() throws Exception {
                // cenario (given)
                Long id = 1l;

                Book book = Book.builder().id(id).title(createNewBook().getTitle()).author(createNewBook().getAuthor())
                                .isbn(createNewBook().getIsbn()).build();

                BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

                // execulcao (when)
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                                .accept(MediaType.APPLICATION_JSON);

                // verificação (then)
                mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("id").value(id))
                                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));

        }

        @Test
        @DisplayName("Deve retornar resource not found quando o livro procurado nao existir")
        public void book_not_found_rest_test() throws Exception {

                BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve deletar um livro.")
        public void delete_book_rest_test() throws Exception {

                BDDMockito.given(service.getById(Mockito.anyLong()))
                                .willReturn(Optional.of(Book.builder().id(1l).build()));

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1));

                mvc.perform(request).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar resource not found quando nao encontrar o livro para deletar.")
        public void delete_inexistent_book_rest_test() throws Exception {

                BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1));

                mvc.perform(request).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve atualizar um livro.")
        public void update_book_rest_test() throws Exception {

                Long id = 1l;
                String json = new ObjectMapper().writeValueAsString(createNewBook());

                Book updatingbook = Book.builder().id(1l).title("some title").author("some auther").isbn("321").build();
                BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingbook));

                Book updatedBook = Book.builder().id(1l).author("Jessi").title("As aventuras").isbn("321").build();

                BDDMockito.given(service.update(updatingbook)).willReturn(updatedBook);

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
                                .content(json).accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON);

                mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("id").value(id))
                                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                                .andExpect(jsonPath("isbn").value("321"));
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente.")
        public void update_inexistent_book_rest_test() throws Exception {

                String json = new ObjectMapper().writeValueAsString(createNewBook());

                BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
                                .content(json).accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON);

                mvc.perform(request).andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Deve filtrar livros")
        public void find_books_rest_test() throws Exception {

                Long id = 1l;
                Book book = Book.builder().id(id).title(createNewBook().getTitle()).author(createNewBook().getAuthor())
                                .isbn(createNewBook().getIsbn()).build();

                BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

                String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(),
                                book.getAuthor());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                                .accept(MediaType.APPLICATION_JSON);

                mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", Matchers.hasSize(1)))
                                .andExpect(jsonPath("totalElements").value(1))
                                .andExpect(jsonPath("pageable.pageSize").value(100))
                                .andExpect(jsonPath("pageable.pageNumber").value(0));
        }

        private BooKDTO createNewBook() {
                return BooKDTO.builder().author("Jessi").title("As aventuras").isbn("001").build();
        }
}
