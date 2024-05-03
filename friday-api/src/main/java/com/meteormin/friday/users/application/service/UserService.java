package com.meteormin.friday.users.application.service;

import com.meteormin.friday.common.hexagon.annotation.Usecase;
import com.meteormin.friday.common.pagination.SimplePage;
import com.meteormin.friday.users.application.exception.ExistsUserException;
import com.meteormin.friday.users.application.exception.NotFoundUserException;
import com.meteormin.friday.users.application.port.in.query.RetrieveUserQuery;
import com.meteormin.friday.users.application.port.in.usecase.UserUsecase;
import com.meteormin.friday.users.application.port.out.UserPort;
import com.meteormin.friday.users.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * User service
 *
 * @author meteormin
 * @since 2023/09/09
 */
@RequiredArgsConstructor
@Usecase
public class UserService implements UserUsecase, RetrieveUserQuery {
    private final UserPort userPort;

    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(CreateUser user) {
        if (userPort.isUniqueEmail(user.email())) {
            throw new ExistsUserException();
        }

        return userPort.createUser(User.create(user));
    }

    /**
     * @param request update user
     * @return updated user
     */
    @Override
    public User patchUser(PatchUser request) {
        User domain = userPort.findById(request.id())
                .orElseThrow(NotFoundUserException::new);
        return userPort.updateUser(domain.patch(request));
    }

    /**
     * Retrieves all users.
     *
     * @return a collection of User objects representing all the users
     */
    @Override
    public List<User> findAll() {
        return userPort.findAll();
    }

    /**
     * Retrieves a page of users based on the specified conditions in the given RetrieveUserCommand.
     *
     * @param request the RetrieveUserCommand object containing the search criteria such as email,
     *        name, created at dates, and updated at dates
     * @return a Page object containing the list of users that match the search criteria
     */
    @Override
    public Page<User> findAll(UserFilter request) {
        Page<User> result;
        Pageable pageable = request.pageable();

        // only paginate
        if (request.isEmpty()) {
            result = userPort.findAll(pageable);
        } else {
            result = userPort.findAll(request);
        }

        return new SimplePage<>(
                result.getContent(),
                result.getTotalElements(),
                result.getPageable(),
                "users");
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the ID of the user to find
     * @return the user with the specified ID
     */
    @Override
    public User findById(Long id) {
        return userPort.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }

    @Override
    public boolean resetPassword(ResetPassword restPassword) {
        User user = userPort.findById(restPassword.id())
                .orElseThrow(NotFoundUserException::new);

        var encPasswd = passwordEncoder.encode(restPassword.password());
        return userPort.resetPassword(
                user.resetPassword(encPasswd)) != null;
    }

    /**
     * Deletes an entity by ID.
     *
     * @param id the ID of the entity to delete
     */
    @Override
    public void deleteUserById(Long id) {
        userPort.deleteById(id);
    }
}
