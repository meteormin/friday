package com.meteormin.friday.users.adapter.in.rest;

import com.meteormin.friday.api.users.UserApi;
import com.meteormin.friday.api.users.request.CreateUserRequest;
import com.meteormin.friday.api.users.request.ResetPasswordRequest;
import com.meteormin.friday.api.users.request.RetrieveUserRequest;
import com.meteormin.friday.api.users.request.UpdateUserRequest;
import com.meteormin.friday.api.users.resource.ResetPasswordResource;
import com.meteormin.friday.api.users.resource.UserResources;
import com.meteormin.friday.api.users.resource.UserResources.UserResource;
import com.meteormin.friday.common.error.RestErrorCode;
import com.meteormin.friday.common.error.RestErrorException;
import com.meteormin.friday.common.hexagon.BaseController;
import com.meteormin.friday.common.hexagon.annotation.RestAdapter;
import com.meteormin.friday.common.request.annotation.QueryParam;
import com.meteormin.friday.infrastructure.security.PrincipalUserInfo;
import com.meteormin.friday.infrastructure.security.annotation.AuthUser;
import com.meteormin.friday.users.application.port.in.query.RetrieveUserQuery;
import com.meteormin.friday.users.application.port.in.usecase.UserUsecase;
import com.meteormin.friday.users.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 *
 * @author meteormin
 * @since 2023/09/02
 */
@RequiredArgsConstructor
@RestAdapter(path = UserApi.PATH)
public class UserController extends BaseController implements UserApi {
    private final UserUsecase userUsecase;
    private final RetrieveUserQuery readUserQuery;

    /**
     * Creates a new user.
     *
     * @param request the createUser request
     * @return the ResponseEntity containing the createUser response
     */
    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<UserResource> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User create = userUsecase.createUser(request.toDomain());

        return ResponseEntity
                .created(createUri("/{id}", create.getId()))
                .body(UserResource.fromDomain(create));
    }

    /**
     * Retrieves a user based on the provided ID.
     *
     * @param id the ID of the user to retrieve
     * @return the ResponseEntity containing the RetrieveUserResponse
     */
    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    public ResponseEntity<UserResource> retrieveUser(
            @PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        if (hasAuthority(id, userInfo)) {
            return ResponseEntity.ok(
                    UserResource.fromDomain(readUserQuery.findById(id)));
        }

        throw new RestErrorException(RestErrorCode.FORBIDDEN);
    }

    /**
     * Retrieves a page of users based on the provided request.
     *
     * @param request the request object containing the parameters for retrieving users
     * @return a ResponseEntity containing a SimplePage of RetrieveUserResponse objects
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<UserResource>> retrieveUsers(
            @QueryParam @Valid RetrieveUserRequest request,
            @PageableDefault(page = 1,
                    sort = "createdAt",
                    direction = Direction.DESC) Pageable pageable) {

        Page<User> users = readUserQuery.findAll(request.toDomain());
        return ResponseEntity.ok(UserResources.fromDomains(users));
    }

    /**
     * Updates a user.
     *
     * @param request the update user request object
     * @return the response entity containing the updated user
     */
    @Override
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    public ResponseEntity<UserResource> patchUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthUser PrincipalUserInfo userInfo) {

        if (hasAuthority(id, userInfo)) {
            var updated = userUsecase.patchUser(request.toDomain(id));
            return ResponseEntity.ok(
                    UserResource.fromDomain(updated));
        }

        throw new RestErrorException(RestErrorCode.FORBIDDEN);
    }

    /**
     * Resets the password for a user.
     *
     * @param id the ID of the user
     * @param password the new password
     * @return the response entity containing the updated user response
     */
    @Override
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyAuthority('admin','user')")
    public ResponseEntity<ResetPasswordResource> resetPassword(
            @PathVariable Long id,
            @RequestBody @Valid ResetPasswordRequest password,
            @AuthUser PrincipalUserInfo userInfo) {
        if (hasAuthority(id, userInfo)) {
            var updated = userUsecase.resetPassword(password.toDomain(id));
            return ResponseEntity.ok(
                    new ResetPasswordResource(updated));
        }
        throw new RestErrorException(RestErrorCode.FORBIDDEN);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to be deleted
     */
    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id,
            @AuthUser PrincipalUserInfo userInfo) {
        if (hasAuthority(id, userInfo)) {
            userUsecase.deleteUserById(id);
            return;
        }

        throw new RestErrorException(RestErrorCode.FORBIDDEN);
    }

    private boolean hasAuthority(Long reqId, PrincipalUserInfo userInfo) {
        return reqId.equals(userInfo.getId())
                || userInfo.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("admin"));
    }
}
