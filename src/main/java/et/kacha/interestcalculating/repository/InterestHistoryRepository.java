package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.InterestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestHistoryRepository extends JpaRepository<InterestHistory, Integer> {

    @Query("select i from InterestHistory i where i.status = ?1")
    List<InterestHistory> findByStatus(InterestPaymentState status);

    @Query("select i from InterestHistory i where i.subscriptions.id = ?1 and i.status = ?2")
    List<InterestHistory> findBySubscriptionsIdAndStatus(Integer id, InterestPaymentState status);

    @Query("select i from InterestHistory i where i.requestRefId = ?1 and i.status = ?2")
    List<InterestHistory> findByRequestRefIdAndStatus(String requestRefId, InterestPaymentState status);


}