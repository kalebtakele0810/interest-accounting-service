package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.TaxHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestTaxHistoryRepository extends JpaRepository<TaxHistory, Integer> {
    @Query("select t from TaxHistory t where t.interestHistory.id = ?1 and t.status = ?2")
    List<TaxHistory> findByInterestHistoryIdAndStatus(Integer id, InterestPaymentState status);
}