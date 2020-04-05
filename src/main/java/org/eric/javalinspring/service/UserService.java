package org.eric.javalinspring.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    public String getUser(int id) {
        return "Eric-" + id;
    }

    public List<String> getUsers() {
        List<String> list = new ArrayList<>();
        list.add("Eric");
        list.add("Kali");
        return list;
    }
}
