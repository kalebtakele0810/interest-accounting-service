package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.entity.InterestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestHistoryRepository extends JpaRepository<InterestHistory, Integer> {
}