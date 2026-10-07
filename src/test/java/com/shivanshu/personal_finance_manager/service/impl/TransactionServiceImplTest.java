package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.response.TransactionListResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.TransactionMapper;
import com.shivanshu.personal_finance_manager.repository.CategoryRepository;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private TransactionRule rule1;

    private final TransactionMapper transactionMapper = new TransactionMapper();

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                transactionRepository,
                categoryRepository,
                currentUserProvider,
                List.of(rule1),
                transactionMapper
        );
    }

    @Test
    void createTransaction_success() {
        User user = new User();
        user.setId(1L);
        when(currentUserProvider.getCurrentUserEntity()).thenReturn(user);

        Category category = new Category("Food", CategoryType.EXPENSE, false, null);
        when(categoryRepository.findAccessibleByName(1L, "Food")).thenReturn(Optional.of(category));

        Transaction savedTx = new Transaction(user, category, new BigDecimal("45.50"), LocalDate.of(2026, 10, 5), "Lunch");
        savedTx.setId(99L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("45.50"),
                LocalDate.of(2026, 10, 5),
                "Food",
                "Lunch"
        );

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(99L, response.id());
        assertEquals(new BigDecimal("45.50"), response.amount());
        assertEquals("Food", response.category());
        assertEquals(CategoryType.EXPENSE, response.type());

        verify(rule1).validate(any(Transaction.class));
    }

    @Test
    void createTransaction_categoryNotFound_throwsBadRequest() {
        User user = new User();
        user.setId(1L);
        when(currentUserProvider.getCurrentUserEntity()).thenReturn(user);
        when(categoryRepository.findAccessibleByName(1L, "NonExistent")).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("10.00"),
                LocalDate.of(2026, 10, 5),
                "NonExistent",
                null
        );

        ApiException ex = assertThrows(ApiException.class, () -> transactionService.createTransaction(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactions_startDateAfterEndDate_throwsBadRequest() {
        LocalDate start = LocalDate.of(2026, 10, 10);
        LocalDate end = LocalDate.of(2026, 10, 5);

        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.getTransactions(start, end, null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTransactions_success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Category cat = new Category("Salary", CategoryType.INCOME, false, null);
        Transaction tx = new Transaction(new User(), cat, new BigDecimal("5000.00"), LocalDate.of(2026, 10, 1), "Paycheck");
        tx.setId(10L);

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(tx));

        TransactionListResponse response = transactionService.getTransactions(null, null, null, null, null);
        assertEquals(1, response.transactions().size());
        assertEquals("Salary", response.transactions().get(0).category());
    }

    @Test
    void updateTransaction_immutableDateChangeAttempt_throwsBadRequest() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Transaction existing = new Transaction();
        existing.setId(10L);
        existing.setTransactionDate(LocalDate.of(2026, 10, 1));
        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        UpdateTransactionRequest request = new UpdateTransactionRequest(
                null,
                null,
                null,
                LocalDate.of(2026, 10, 2)
        );

        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.updateTransaction(10L, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Transaction date cannot be changed", ex.getMessage());
    }

    @Test
    void updateTransaction_notFound_throwsNotFound() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        UpdateTransactionRequest request = new UpdateTransactionRequest(new BigDecimal("20.00"), null, null, null);

        ApiException ex = assertThrows(ApiException.class,
                () -> transactionService.updateTransaction(999L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteTransaction_success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Transaction existing = new Transaction();
        existing.setId(10L);
        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(10L));
        verify(transactionRepository).delete(existing);
    }
}
