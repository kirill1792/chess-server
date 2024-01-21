package ru.kirill.chess.repository;

import ru.kirill.chess.model.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Long>{
}
