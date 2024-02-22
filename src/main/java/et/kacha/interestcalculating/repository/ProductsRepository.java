package et.kacha.interestcalculating.repository;


import et.kacha.interestcalculating.constants.InterestCompType;
import et.kacha.interestcalculating.constants.ProductState;
import et.kacha.interestcalculating.constants.ProductType;
import et.kacha.interestcalculating.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {

    @Query("select p from Products p where p.interest_comp_type = ?1 and p.state = ?2 and p.product_type = ?3")
    List<Products> findByInterestCompTypeAndProductstate(InterestCompType interest_comp_type, ProductState state, ProductType product_type);

    @Query("select p from Products p where p.product_type = ?1 and p.state = ?2")
    List<Products> findByProductTypeAndState(ProductType product_type, ProductState state);

    @Query("select p from Products p where p.financial_institution_id = ?1 and p.isOrdinary = ?2 and p.state = ?3")
    List<Products> findByFinancial_institution_idAndIsOrdinaryAndState(Integer financial_institution_id, Boolean isOrdinary, ProductState state);
}
