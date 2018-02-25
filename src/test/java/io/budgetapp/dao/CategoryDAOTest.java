package io.budgetapp.dao;

import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Category;
import io.budgetapp.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CategoryDAOTest {

    private CategoryDAO categoryDao;
    private SessionFactory mockSessionFactory;
    private AppConfiguration mockConfiguration;
    private Session mockSession;
    private User user;

    @Before
    public void setup(){
        this.mockSessionFactory = mock(SessionFactory.class);
        this.mockConfiguration = mock(AppConfiguration.class);
        this.categoryDao = new CategoryDAO(mockSessionFactory, mockConfiguration);
        this.user = mock(User.class);
        this.mockSession = mock(Session.class);

        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    public void test_Add_Category(){
        //setup
        Category mockCategory = mock(Category.class);
        
        //call
        this.categoryDao.addCategory(this.user, mockCategory);
        //verify
        
        verify(mockCategory).setUser(this.user);
        verify(this.mockSession).saveOrUpdate(mockCategory);
    }


    @Test
    public void test_Delete_Category(){
        //setup
        Category mockCategory = mock(Category.class);

        this.categoryDao.delete(mockCategory);

        //verify
        verify(this.mockSession).delete(mockCategory);
    }

}
