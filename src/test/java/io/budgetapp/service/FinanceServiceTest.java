package io.budgetapp.service;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.*;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.recurring.AddRecurringForm;
import io.budgetapp.model.form.user.Password;
import io.budgetapp.model.form.user.Profile;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.budgetapp.model.form.budget.UpdateBudgetForm;
import io.budgetapp.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

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

    @Test
    public void testAddValidRecurring(){
        // Set up
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock,
                                                           categoryDAOMock, transactionDAOMock, recurringDAOMock,
                                                           authTokenDAOMock, passwordEncoderMock);
        User testUser = new User();
        AddRecurringForm addRecurringFormMock = mock( AddRecurringForm.class );
        Budget mockBudget = Mockito.mock( Budget.class );
        Date now = new Date();
        Recurring recurring;
        Recurring recurringMock = Mockito.mock( Recurring.class );

        // Stub
        when( financeService.findBudgetById( testUser, 0 ) ).thenReturn( mockBudget );
        when( addRecurringFormMock.getRecurringAt() ).thenReturn( new Date() );
        when( addRecurringFormMock.getAmount() ).thenReturn( 10.00 );
        when( addRecurringFormMock.getRecurringType() ).thenReturn( RecurringType.MONTHLY );
        when( mockBudget.getBudgetType() ).thenReturn( new BudgetType() );
        when( addRecurringFormMock.getRemark() ).thenReturn( "" );
        when( mockBudget.getName() ).thenReturn( "" );
        when( recurringDAOMock.addRecurring( any() ) ).thenReturn( recurringMock );
        when( recurringMock.getAmount() ).thenReturn( 10.00 );

        // Pre-lim date check
        Assert.assertTrue( Util.inMonth( addRecurringFormMock.getRecurringAt(), now ) );

        // Call
        recurring = financeService.addRecurring( testUser, addRecurringFormMock );

        //Verification: Check if actually added Recurring
        Assert.assertNotNull( recurring );
    }

    @Test
    public void testDeleteRecurring(){
        //Set up
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock,
                                                           categoryDAOMock, transactionDAOMock, recurringDAOMock,
                                                           authTokenDAOMock, passwordEncoderMock);
        User testUser = new User();
        Long recurringId = 100L;
        Recurring toDelete = new Recurring();

        // Stubs
        when( recurringDAOMock.find( testUser, recurringId ) ).thenReturn( toDelete );

        // Call
        financeService.deleteRecurring( testUser, 100 );

        // Verification: Proof of deletion
        Assert.assertNull( toDelete.getId() );
        Assert.assertNotNull( recurringId );
    }
}
;
