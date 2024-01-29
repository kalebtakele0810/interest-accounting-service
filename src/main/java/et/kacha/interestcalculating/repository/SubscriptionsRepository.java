package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Integer> {

    @Query("select s from Subscriptions s where s.product.id = ?1")
    List<Subscriptions> findByProductId(Integer id);
}
