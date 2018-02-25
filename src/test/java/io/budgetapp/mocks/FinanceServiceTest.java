package io.budgetapp.mocks;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.budgetapp.model.form.budget.UpdateBudgetForm;
import io.budgetapp.service.FinanceService;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

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
        SignUpForm form = new SignUpForm();
        form.setUsername("usernameExists");
        form.setPassword("password");

        //mock
        User userMock = mock(User.class);
        Optional<User> userOptional = Optional.of(userMock);

        //stub
        when(userDAOMock.findByUsername("usernameExists")).thenReturn(userOptional);

        //when
        financeService.addUser(form);

        //then exception is caught via the @Test annotation

    }

    //verifies that the user created matches the sign up form
    @Test
    public void addUserTest(){
        //given
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        SignUpForm form = new SignUpForm();
        

        //spy
        FinanceService financeServiceSpy = spy(financeService);
        SignUpForm formSpy = spy(form);
        formSpy.setUsername("myUsername");
        formSpy.setPassword("myPassword");

        //when
        financeServiceSpy.addUser(formSpy);

        //verify
        verify(userDAOMock).findByUsername(formSpy.getUsername());
        verify(formSpy).setPassword(anyString());
        verify(userDAOMock).add(formSpy);

    }

    @Test
    public void test_addBudget(){
        //setup
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User mockUser = mock(User.class);
        AddBudgetForm mockAddBudgetForm = mock(AddBudgetForm.class);

        //call
        financeService.addBudget(mockUser, mockAddBudgetForm);

        //verify
        verify(this.budgetTypeDAOMock).addBudgetType();
        verify(this.budgetDAOMock).addBudget(any(User.class), any(Budget.class));
    }

    @Test
    public void test_updateBudget(){
        //setup
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User mockUser = mock(User.class);
        UpdateBudgetForm mockUpdateBudgetForm = mock(UpdateBudgetForm.class);
        Budget mockBudget = mock(Budget.class);
        Category mockCategory = mock(Category.class);

        //Stub
        when(this.budgetDAOMock.findById(any(User.class), anyLong())).thenReturn(mockBudget);

        when(mockUpdateBudgetForm.getId()).thenReturn(1L);
        when(mockUpdateBudgetForm.getName()).thenReturn("TESTING");
        when(mockUpdateBudgetForm.getProjected()).thenReturn(1D);
        when(mockBudget.getCategory()).thenReturn(mockCategory);
        when(mockCategory.getId()).thenReturn(1L);
        when(mockCategory.getType()).thenReturn(null);

        when(this.categoryDAOMock.findById(anyLong())).thenReturn(mockCategory);

        //call
        financeService.updateBudget(mockUser, mockUpdateBudgetForm);

        //Verify
        verify(this.budgetDAOMock).findById(mockUser, mockUpdateBudgetForm.getId());
        verify(this.categoryDAOMock).findById(1L);
        verify(mockBudget).setName(anyString());
        verify(mockBudget).setProjected(anyDouble());
        verify(this.budgetDAOMock).update(mockBudget);
    }
}
;