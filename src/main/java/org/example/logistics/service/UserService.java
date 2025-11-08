package org.example.logistics.service;


import org.example.logistics.mapper.ClientMapper;
import org.example.logistics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientMapper clientMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();




}
