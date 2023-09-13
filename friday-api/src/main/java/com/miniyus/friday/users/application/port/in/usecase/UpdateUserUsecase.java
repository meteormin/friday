package com.miniyus.friday.users.application.port.in.usecase;

import com.miniyus.friday.users.domain.User;

/**
 * [description]
 *
 * @author miniyus
 * @date 2023/09/09
 */
public interface UpdateUserUsecase {
    User patchUser(UpdateUserCommand command);

    User resetPassword(Long id, String password);
}
