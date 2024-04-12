package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.InterestCompType;
import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.InterestHistory;
import et.kacha.interestcalculating.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InterestHistoryRepository extends JpaRepository<InterestHistory, Integer> {
    @Query("select i from InterestHistory i where i.status = ?1")
    List<InterestHistory> findByStatus(InterestPaymentState status);

    @Query("select i from InterestHistory i where i.subscriptions.id = ?1 and i.status = ?2")
    List<InterestHistory> findBySubscriptionsIdAndStatus(Integer id, InterestPaymentState status);

    @Query("select i from InterestHistory i where i.requestRefId = ?1 and i.status = ?2")
    List<InterestHistory> findByRequestRefIdAndStatus(String requestRefId, InterestPaymentState status);

    @Query("select i from InterestHistory i where i.status = ?1 and i.subscriptions.product.interest_comp_type=?2")
    List<InterestHistory> findByCompTypeStatus(InterestPaymentState status, InterestCompType interest_comp_type);

    @Transactional
    @Modifying
    @Query("update InterestHistory i set i.status = ?1 where i.subscriptions = ?2 and i.status = ?3")
    int updateStatusBySubscriptionsAndStatus(InterestPaymentState status, Subscriptions subscriptions, InterestPaymentState status1);

}