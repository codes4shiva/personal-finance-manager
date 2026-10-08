package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.response.TransactionListResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.*;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.TransactionMapper;
import com.shivanshu.personal_finance_manager.repository.CategoryRepository;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.TransactionService;
import com.shivanshu.personal_finance_manager.service.rule.TransactionRule;
import com.shivanshu.personal_finance_manager.specification.TransactionSpecifications;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Concrete implementation of TransactionService managing CRUD operations,
 * specification filtering, and rule-based validation.
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;
    private final List<TransactionRule> rules;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            CurrentUserProvider currentUserProvider,
            List<TransactionRule> rules,
            TransactionMapper transactionMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
        this.rules = rules;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UserEntity userEntity = currentUserProvider.getCurrentUserEntity();
        Category category = categoryRepository.findAccessibleByName(userEntity.getId(), request.category().trim())
                .orElseThrow(() -> ApiException.badRequest("Category '" + request.category() + "' not found"));

        Transaction transaction = new Transaction(
                userEntity,
                category,
                MoneyUtils.scale(request.amount()),
                request.date(),
                request.description() != null ? request.description().trim() : null
        );

        for (TransactionRule rule : rules) {
            rule.validate(transaction);
        }

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            String categoryName,
            CategoryType type
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw ApiException.badRequest("startDate cannot be after endDate");
        }

        Long userId = currentUserProvider.getCurrentUserId();

        Specification<Transaction> spec = TransactionSpecifications.forUser(userId)
                .and(TransactionSpecifications.startDateOnOrAfter(startDate))
                .and(TransactionSpecifications.endDateOnOrBefore(endDate))
                .and(TransactionSpecifications.hasCategoryId(categoryId))
                .and(TransactionSpecifications.hasCategoryName(categoryName))
                .and(TransactionSpecifications.hasType(type));

        Sort sort = Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"));
        List<Transaction> transactions = transactionRepository.findAll(spec, sort);

        List<TransactionResponse> responses = transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

        return new TransactionListResponse(responses);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserEntityId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Transaction not found"));

        if (request.date() != null && !request.date().equals(transaction.getTransactionDate())) {
            throw ApiException.badRequest("Transaction date cannot be changed");
        }

        if (request.amount() != null) {
            transaction.setAmount(MoneyUtils.scale(request.amount()));
        }

        if (request.description() != null) {
            transaction.setDescription(request.description().trim());
        }

        if (request.category() != null && !request.category().isBlank()) {
            Category category = categoryRepository.findAccessibleByName(userId, request.category().trim())
                    .orElseThrow(() -> ApiException.badRequest("Category '" + request.category() + "' not found"));
            transaction.setCategory(category);
        }

        for (TransactionRule rule : rules) {
            rule.validate(transaction);
        }

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserEntityId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Transaction not found"));

        transactionRepository.delete(transaction);
    }
}
