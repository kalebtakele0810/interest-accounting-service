package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.entity.Whitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {

    @Query("select w from Whitelist w where w.phone = ?1 and w.products.id = ?2")
    Whitelist findByPhoneAndProducts_Id(String phone, Integer id);

    @Query("select w from Whitelist w where w.fi_id = ?1")
    List<Whitelist> findByFiId(Integer fi_id);


}
