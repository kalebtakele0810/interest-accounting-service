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
/*
    @Query("select p from Products p where p.interest_type = ?1 and p.state = ?2")
    List<Products> findByInterest_typeAndProductstate(String interest_type, String productstate);
    */


    @Query("select p from Products p where p.interest_comp_type = ?1 and p.state = ?2 and p.product_type = ?3")
    List<Products> findByInterestCompTypeAndProductstate(InterestCompType interest_comp_type, ProductState state, ProductType product_type);

    @Query("select p from Products p where p.product_type = ?1 and p.state = ?2")
    List<Products> findByProduct_typeAndState(ProductType product_type, ProductState state);
}
