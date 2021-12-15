package com.example.demo.Controllers;

import com.example.demo.Controllers.TestUtils;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserControllerTest {

    private UserController userController;

    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);


    @Mock
    private UserRepository userRepo =mock(UserRepository.class);

    @Mock
    private CartRepository cartRepo =mock(CartRepository.class);

    @Before
    public void setup() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository",userRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder",encoder);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
    }

    @Test
    public void createUserTest(){
            when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
            CreateUserRequest r = new CreateUserRequest();
            r.setUsername("test");
            r.setPassword("testPassword");
            r.setConfirmPassword("testPassword");

            final ResponseEntity<User> response = userController.createUser(r);

            assertNotNull(response);
            assertEquals(200, response.getStatusCodeValue());

            User u = response.getBody();
            assertNotNull(u);
            assertEquals(0, u.getId());
            assertEquals("test", u.getUsername());
            assertEquals("thisIsHashed", u.getPassword());
    }

    @Test
    public void checkPasswordSpecification(){
        CreateUserRequest r = new CreateUserRequest();
        r.setUsername("test");
        r.setPassword("abcde");
        r.setConfirmPassword("abcde");

        final ResponseEntity<User> response = userController.createUser(r);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void test_find_by_userName(){
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("test");
        req.setPassword("abc");
        req.setConfirmPassword("abc");
        User u= new User();
        u.setUsername("test");
        u.setId(1);
        when(userRepo.findByUsername(Matchers.anyString())).thenReturn(u);
        ResponseEntity<User> userRepsonse= userController.findByUserName("test");
        assertEquals("test",userRepsonse.getBody().getUsername());

    }

    @Test
    public void test_find_by_userId(){
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("test");
        req.setPassword("abc");
        req.setConfirmPassword("abc");
        User u= new User();
        u.setUsername("test");
        u.setId(Long.valueOf(1));
        when(userRepo.findById(Matchers.anyLong())).thenReturn(java.util.Optional.of(u));
        ResponseEntity<User> userRepsonse= userController.findById(Long.valueOf(1));
        assertEquals(1l,userRepsonse.getBody().getId());

    }


}
