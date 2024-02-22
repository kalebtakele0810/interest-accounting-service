package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.constants.ChargeState;
import et.kacha.interestcalculating.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargesRepository extends JpaRepository<Charge, Integer> {

//    @Query("select c from Charge c where c.products.id = ?1")
//    List<Charge> findByProductsId(Integer id);

    @Query("select c from Charge c where c.products.id = ?1 and c.status = ?2")
    List<Charge> findByProductsIdAndStatus(Integer id, ChargeState status);

}
