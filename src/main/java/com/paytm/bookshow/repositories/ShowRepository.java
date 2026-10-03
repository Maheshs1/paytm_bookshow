package com.paytm.bookshow.repositories;


import com.paytm.bookshow.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {
}
