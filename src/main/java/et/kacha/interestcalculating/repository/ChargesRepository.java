package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.constants.ChargeFor;
import et.kacha.interestcalculating.constants.ChargeState;
import et.kacha.interestcalculating.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargesRepository extends JpaRepository<Charge, Integer> {
    @Query("select c from Charge c where c.products.id = ?1 and c.status = ?2")
    List<Charge> findByProductsIdAndStatus(Integer id, ChargeState status);

    @Query("select c from Charge c where c.products.id = ?1 and c.status = ?2 and c.charge_for = ?3")
    List<Charge> findByProductsIdAndStatusAndChargeFor(Integer id, ChargeState status, ChargeFor charge_for);

}
