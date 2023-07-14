package ru.itmentor.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmentor.spring.boot_security.demo.model.Role;
import ru.itmentor.spring.boot_security.demo.model.User;
import ru.itmentor.spring.boot_security.demo.service.implementation.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class AllRequestsRestController {
    @Autowired
    private UserService userService;

    @GetMapping(path = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> selectAll() {
        List<User> users = userService.getAll();
        return users != null && !users.isEmpty()
                ? new ResponseEntity<>(users, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> selectById(@PathVariable("id") long id) {
        Optional<User> user = userService.getById(id);
        return user.map(
                value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody User userReqBody) {
        Optional<User> userFromDb = userService.getByParam(userReqBody.getUsername());

        if (userFromDb.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = new User(
                userReqBody.getName(),
                userReqBody.getLastname(),
                userReqBody.getAge(),
                userReqBody.getUsername(),
                userReqBody.getPassword());

        if (userReqBody.getRoles() != null) {
            Arrays.stream(userReqBody.getRoles().toArray(Role[]::new)).forEach(role->userService.addRoleByService(user, role));
        }

        userService.save(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(path = "/{id}/update",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> update(@RequestBody User userReqBody,
                                       @PathVariable("id") long id) {
        User user = userService.getById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(userReqBody.getName());
        user.setLastname(userReqBody.getLastname());
        user.setAge(userReqBody.getAge());

        if (userReqBody.getRoles() != null) {
            Arrays.stream(userReqBody.getRoles().toArray(Role[]::new)).forEach(role->userService.addRoleByService(user, role));
        }

        userService.update(id, userReqBody);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<User> delete(@PathVariable("id") int id) {
        userService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
