package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.ProductState;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
    @Query("""
            select t from Transactions t
            where t.subscriptions.product.id = ?1 and t.subscriptions.customer.id = ?2 and t.subscriptions.product.state = ?3 and t.status = ?4""")
    List<Transactions> findByProductIdAndCustomerIdAndStatus(Integer id, Integer id1, ProductState state, TransactionStatus status);

}

