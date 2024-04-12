package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.USSDStatus;
import et.kacha.interestcalculating.entity.CashoutPendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashoutPendingTransactionRepo extends JpaRepository<CashoutPendingTransaction, Integer> {

    @Query("select c from CashoutPendingTransaction c where c.status = ?1")
    List<CashoutPendingTransaction> findByStatus(USSDStatus status);
}
