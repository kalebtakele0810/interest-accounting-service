package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {
    @Query("select p from Products p where p.status = ?1")
    List<Products> findByStatus(String status);
}
