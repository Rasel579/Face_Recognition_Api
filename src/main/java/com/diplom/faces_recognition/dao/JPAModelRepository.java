package com.diplom.faces_recognition.dao;

import com.diplom.faces_recognition.dao.entities.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JPAModelRepository extends JpaRepository<ModelMapper, String> {

    @Query("SELECT u FROM ModelMapper u")
    List<ModelMapper> getAllUsers();

    @Modifying
    @Query(value = "insert into face_db.USERS (login, password) VALUES (:login ,crypt(:password, gen_salt('md5')))", nativeQuery = true)
    @Transactional
    void saveUser( String login, String password);

    @Query(value = "SELECT ( password = crypt( :password, password ) ) as password_matcher FROM face_db.USERS where login = :login", nativeQuery = true)
    boolean verifiedUser( String login, String password);
}
