package com.shivanshu.personal_finance_manager.mapper;

import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

/**
 * Mapper converting between Transaction entities and DTO representations.
 */
@Component
public class TransactionMapper {

    /**
     * Converts a Transaction entity to a TransactionResponse DTO.
     *
     * @param transaction The entity to convert
     * @return TransactionResponse DTO
     */
    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionResponse(
                transaction.getId(),
                MoneyUtils.scale(transaction.getAmount()),
                transaction.getTransactionDate(),
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getDescription(),
                transaction.getType()
        );
    }
}
