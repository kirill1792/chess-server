package ru.kirill.chess.repository;

import ru.kirill.chess.model.User1;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User1, Long>{
}
