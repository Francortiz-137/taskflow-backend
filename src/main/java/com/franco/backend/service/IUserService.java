package com.franco.backend.service;

import com.franco.backend.dto.UserDTO;
import java.util.List;

public interface IUserService {

    UserDTO create(UserDTO dto);

    UserDTO getById(Long id);

    List<UserDTO> getAll();

    UserDTO update(Long id, UserDTO dto);

    void delete(Long id);
}
