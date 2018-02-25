package io.budgetapp.mocks;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.user.Password;
import io.budgetapp.model.form.user.Profile;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.budgetapp.model.form.budget.UpdateBudgetForm;
import io.budgetapp.service.FinanceService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void updateTest(){
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User user = new User();
        Profile profile = new Profile();
        profile.setName("testName");
        profile.setCurrency("cad");

        //action
        User updatedUser = financeService.update(user, profile);

        //result
        verify(userDAOMock).update(user);
        assertEquals(updatedUser.getName(), profile.getName());
        assertEquals(updatedUser.getCurrency(), profile.getCurrency());
    }

    @Test(expected=DataConstraintException.class)
    public void changePasswordInconsistentPasswordTestConfirm(){
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User user = new User();
        Password password = new Password();
        password.setPassword("test");
        password.setConfirm("fail");

        //when
        financeService.changePassword(user, password);

        //then exception is caught via the @Test annotation
        assertFalse(password.getPassword().equals(password.getConfirm()));
    }

    @Test(expected=DataConstraintException.class)
    public void changePasswordInconsistentPasswordTestOriginal(){
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User user = new User();
        user.setPassword("fail");
        Password password = new Password();
        password.setPassword("test");
        password.setConfirm("test");
        password.setOriginal("test");

        //when
        when(passwordEncoderMock.matches(password.getOriginal(), user.getPassword())).thenReturn(false);
        financeService.changePassword(user, password);

        //then exception is caught via the @Test annotation
        assertTrue(password.getPassword().equals(password.getConfirm()));
        assertFalse(password.getOriginal().equals(user.getPassword()));
    }

    @Test
    public void changePasswordConsistentPasswordTest() {
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User userReal = new User();
        userReal.setUsername("dummy user");
        userReal.setPassword("dummy pass");

        User userMock = mock(User.class);

        Password password = new Password();
        password.setPassword("test");
        password.setConfirm("test");
        password.setOriginal("dummy pass");

        //when
        when(userDAOMock.findById(anyLong())).thenReturn(userReal);
        when(userMock.getPassword()).thenReturn("dummy pass");
        when(passwordEncoderMock.matches(password.getOriginal(), userReal.getPassword())).thenReturn(true);
        when(passwordEncoderMock.encode(anyString())).thenReturn("test");
        financeService.changePassword(userMock, password);

        //
        assertTrue(password.getPassword().equals(password.getConfirm()));
        assertTrue(password.getOriginal().equals(userMock.getPassword()));
        verify(userDAOMock).update(userReal);
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
