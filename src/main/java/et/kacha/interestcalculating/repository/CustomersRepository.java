package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CustomersRepository extends JpaRepository<Customers, Integer> {
}
