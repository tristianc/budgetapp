package io.budgetapp.mocks;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.Budget;
import io.budgetapp.model.RecurringType;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.report.SearchFilter;
import io.budgetapp.service.FinanceService;
import org.hibernate.Criteria;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class FinanceServiceTest {

    private UserDAO userDAOMock;
    private BudgetDAO budgetDAOMock;
    private BudgetTypeDAO budgetTypeDAOMock;
    private CategoryDAO categoryDAOMock;
    private TransactionDAO transactionDAOMock;
    private RecurringDAO recurringDAOMock;
    private AuthTokenDAO authTokenDAOMock;
    private PasswordEncoder passwordEncoderMock;
    private SignUpForm signUpFormMock;

    @Before
    public void initMocks(){
        userDAOMock = mock(UserDAO.class);
        budgetDAOMock = mock(BudgetDAO.class);
        budgetTypeDAOMock = mock(BudgetTypeDAO.class);
        categoryDAOMock = mock(CategoryDAO.class);
        transactionDAOMock = mock(TransactionDAO.class);
        recurringDAOMock = mock(RecurringDAO.class);
        authTokenDAOMock = mock(AuthTokenDAO.class);
        passwordEncoderMock = mock(PasswordEncoder.class);
        signUpFormMock = mock(SignUpForm.class);
    }


    //verifies that an exception is caught when adding a user with an existing username
    @Test(expected=DataConstraintException.class)
    public void addUserExistingUserNameTest(){
        //given
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User userMock = mock(User.class);
        Optional<User> userOptional = Optional.of(userMock);
        SignUpForm form = new SignUpForm();
        form.setUsername("usernameExists");
        form.setPassword("password");
        when(userDAOMock.findByUsername("usernameExists")).thenReturn(userOptional);

        //when
        financeService.addUser(form);

        //then exception is caught via the @Test annotation
    }

    @Test
    public void deleteTransactionTest(){
        // setup
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User mockUser = mock(User.class);
        Transaction mockTransaction = mock(Transaction.class);
        Budget mockBudget = mock(Budget.class);

        Long id = (long)1;
        Double actual = 4.0;
        Double amount = 2.0;

        // stubs
        when(transactionDAOMock.findById(mockUser, id)).thenReturn(Optional.ofNullable(mockTransaction));
        when(mockTransaction.getId()).thenReturn(id);
        when(mockTransaction.getBudget()).thenReturn(mockBudget);
        when(mockBudget.getActual()).thenReturn(actual);
        when(mockTransaction.getAmount()).thenReturn(amount);

        // call
        Boolean deleteTransaction = financeService.deleteTransaction(mockUser, mockTransaction.getId());

        // verify
        verify(transactionDAOMock).delete(mockTransaction);
        assertTrue(deleteTransaction);
    }

    @Test
    public void findTransactionByIdTest(){
        // setup
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        Long iD = (long)2;

        // call
        financeService.findTransactionById(iD);

        // verify
        verify(transactionDAOMock).findById(iD);
    }

    @Test
    public void findTodayRecurringsTransactionsTest(){
        // setup
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User testUser = new User();
        SearchFilter testSearchFilter = new SearchFilter();
        testSearchFilter.setStartOn(new Date());
        testSearchFilter.setEndOn(new Date());
        testSearchFilter.setAuto(Boolean.TRUE);

        // call
        List returnlist = financeService.findTodayRecurringsTransactions(testUser);

        // verify
        assertTrue(returnlist instanceof List);
    }
}
