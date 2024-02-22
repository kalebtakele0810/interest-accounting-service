package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.constants.ChargeState;
import et.kacha.interestcalculating.entity.Charge;
import et.kacha.interestcalculating.entity.ChargeFees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeFeesRepository extends JpaRepository<ChargeFees, Integer> {

//    @Query("select c from ChargeFees c where c.charge.id = ?1")
//    List<ChargeFees> findByChargeId(Integer id);

    @Query("select c from ChargeFees c where c.charge.id = ?1 and c.status = ?2")
    List<ChargeFees> findByChargeIdAndStatus(Integer id, ChargeState status);

}
