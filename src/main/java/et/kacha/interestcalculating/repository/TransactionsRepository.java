package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {

    /*@Query("select s from Subscriptions s where s.product.id = ?1 and s.customer.id = ?2")
    List<Subscriptions> findByProduct_IdAndCustomer_Id(Integer id, Integer id1);*/

    @Query("select s from Subscriptions s where s.product.id = ?1 and s.customer.id = ?2 order by s.updated_at_time")
    List<Transactions> findByProductIdAndCustomerId(Integer id, Integer id1);
}

