package com.bookverse.domain.book.service;

import com.bookverse.domain.book.dto.BookCreateRequest;
import com.bookverse.domain.book.dto.BookResponse;
import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.repository.BookRepository;
import com.bookverse.domain.category.entity.Category;
import com.bookverse.domain.category.repository.CategoryRepository;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 도서 서비스 단위 테스트
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("도서 등록 성공")
    void createBook_Success() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder()
                .id(categoryId)
                .name("IT/컴퓨터")
                .build();

        BookCreateRequest request = new BookCreateRequest(
                "클린 코드",
                "로버트 C. 마틴",
                "9788966260959",
                "인사이트",
                LocalDate.of(2013, 1, 1),
                33000,
                100,
                "애자일 소프트웨어 장인 정신",
                "cover.jpg",
                categoryId
        );

        Book savedBook = request.toEntity(category);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(bookRepository.save(any(Book.class))).willReturn(savedBook);

        // when
        Long bookId = bookService.createBook(request);

        // then
        verify(categoryRepository).findById(categoryId);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 도서 등록 시 예외 발생")
    void createBook_CategoryNotFound() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "클린 코드",
                "로버트 C. 마틴",
                "9788966260959",
                "인사이트",
                LocalDate.of(2013, 1, 1),
                33000,
                100,
                "애자일 소프트웨어 장인 정신",
                "cover.jpg",
                999L
        );

        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("도서 조회 성공")
    void getBook_Success() {
        // given
        Long bookId = 1L;
        Category category = Category.builder().id(1L).name("IT/컴퓨터").build();
        Book book = Book.builder()
                .id(bookId)
                .title("클린 코드")
                .author("로버트 C. 마틴")
                .isbn("9788966260959")
                .publisher("인사이트")
                .publishedDate(LocalDate.of(2013, 1, 1))
                .price(33000)
                .stockQuantity(100)
                .category(category)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

        // when
        BookResponse response = bookService.getBook(bookId);

        // then
        assertThat(response.getTitle()).isEqualTo("클린 코드");
        assertThat(response.getAuthor()).isEqualTo("로버트 C. 마틴");
    }
}
