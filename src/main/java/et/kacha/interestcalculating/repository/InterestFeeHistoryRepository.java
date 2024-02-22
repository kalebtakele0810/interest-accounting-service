package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.InterestFeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestFeeHistoryRepository extends JpaRepository<InterestFeeHistory, Integer> {

    @Query("select i from InterestFeeHistory i where i.interestHistory.id = ?1 and i.status = ?2")
    List<InterestFeeHistory> findByInterestHistoryIdAndStatus(Integer id, InterestPaymentState status);
}