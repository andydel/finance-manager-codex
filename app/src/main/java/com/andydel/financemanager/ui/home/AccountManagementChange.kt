package com.andydel.financemanager.ui.home

import com.andydel.financemanager.domain.model.AccountType

data class AccountManagementChange(
    val reorderedAccountIdsByType: Map<AccountType, List<Long>>,
    val deletedAccountIds: List<Long>
)
