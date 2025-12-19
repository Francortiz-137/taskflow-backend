package com.franco.backend.service.impl;

import com.franco.backend.dto.UserDTO;
import com.franco.backend.entity.User;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.service.IUserService;
import com.franco.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Override
    public UserDTO create(UserDTO dto) {
        User user = mapper.toEntity(dto);
        return mapper.toDTO(repo.save(user));
    }

    @Override
    public UserDTO getById(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.toDTO(u);
    }

    @Override
    public List<UserDTO> getAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public UserDTO update(Long id, UserDTO dto) {
        User u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        u.setName(dto.getName());
        u.setEmail(dto.getEmail());

        return mapper.toDTO(repo.save(u));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
