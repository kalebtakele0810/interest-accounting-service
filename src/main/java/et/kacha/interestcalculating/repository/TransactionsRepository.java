package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.ProductState;
import et.kacha.interestcalculating.constants.SubscriptionStatus;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
  /*  @Query("""
            select t from Transactions t
            where t.subscriptions.product.id = ?1 and t.subscriptions.customer.id = ?2 and t.subscriptions.product.state = ?3 and t.status = ?4""")
    List<Transactions> findByProductIdAndCustomerIdAndStatus(Integer id, Integer id1, ProductState state, TransactionStatus status);
*/
    @Query("""
            select t from Transactions t
            where t.subscriptions.product.id = ?1 and t.subscriptions.customer.id = ?2 and t.subscriptions.product.state = ?3 and t.status = ?4 and t.subscriptions.status = ?5 and t.is_archived = ?6 """)
    List<Transactions> findByProductIdAndCustomerIdAndStatus(Integer id, Integer id1, ProductState state, TransactionStatus status, SubscriptionStatus status1, Boolean is_archived);

  @Transactional
  @Modifying
  @Query("update Transactions t set t.status = ?1 where t.subscriptions = ?2")
  int updateStatusBySubscriptions(TransactionStatus status, Subscriptions subscriptions);

  @Transactional
  @Modifying
  @Query("update Transactions t set t.is_archived = ?1 where t.subscriptions = ?2")
  int updateIs_archivedBySubscriptions(Boolean is_archived, Subscriptions subscriptions);

}

