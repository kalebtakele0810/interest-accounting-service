package et.kacha.interestcalculating.repository;

import et.kacha.interestcalculating.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TransactionsRepository extends JpaRepository<Subscriptions, Integer> {
}

