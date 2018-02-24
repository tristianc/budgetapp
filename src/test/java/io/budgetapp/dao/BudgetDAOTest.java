package io.budgetapp.dao;

import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Budget;
import io.budgetapp.model.User;
import io.budgetapp.util.Util;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class BudgetDAOTest {

    private BudgetDAO budgetDao;
    private SessionFactory mockSessionFactory;
    private AppConfiguration mockConfiguration;
    private Session mockSession;
    private User user;

    @Before
    public void setup(){
        this.mockSessionFactory = mock(SessionFactory.class);
        this.mockConfiguration = mock(AppConfiguration.class);
        this.budgetDao = new BudgetDAO(mockSessionFactory, mockConfiguration);
        this.user = mock(User.class);
        this.mockSession = mock(Session.class);

        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    public void test_AddBudget(){
        //setup
        Budget mockBudget = mock(Budget.class);
        //stub
        when(mockBudget.getPeriod()).thenReturn(null);

        //call
        this.budgetDao.addBudget(this.user, mockBudget);
        //verify
        verify(mockBudget).setPeriod(Util.currentYearMonth());
        verify(mockBudget).setUser(this.user);
        verify(this.mockSession).saveOrUpdate(mockBudget);
    }

    @Test
    public void test_updateBudget(){
        //setup
        Budget mockBudget = mock(Budget.class);

        this.budgetDao.update(mockBudget);

        //verify
        verify(this.mockSession).saveOrUpdate(mockBudget);
    }

    @Test
    public void test_deleteBudget(){
        //setup
        Budget mockBudget = mock(Budget.class);

        this.budgetDao.delete(mockBudget);

        //verify
        verify(this.mockSession).delete(mockBudget);
    }


}
