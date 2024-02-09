package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "saving_products")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(generator = "saving_products_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String product_name;

    @Column(nullable = false)
    private String product_description;

    @Column(nullable = false)
    private Integer decimal_places;

    @Column(nullable = false)
    private float min_deposit_amt;

    @Column(nullable = false)
    private float max_deposit_amt;

    @Column(nullable = false)
    private float annum_interest_rate;

    @Column(nullable = false)
    private Integer term_duration;

    //DAILY,MONTHLY
    @Enumerated(EnumType.STRING)
    private InterestCompType interest_comp_type;

    @Column(nullable = false)
    private Integer interest_posting_period;

    //MIN_BALANCE,AVERAGE
    @Enumerated(EnumType.STRING)
    private InterestCalculatedUsing interest_calculated_using;

    @Column(nullable = false)
    private float min_opening_balance;

    @Column(nullable = false)
    private float max_saving_limit;

    @Column(nullable = false)
    private Integer min_deposit_term;

    @Column(nullable = false)
    private Integer max_deposit_term;

    @Column(nullable = true)
    private Boolean is_tax_available;

    //FLAT,PERCENTAGE
    @Enumerated(EnumType.STRING)
    private ChargeRate tax_fee_type;

    @Column(nullable = false)
    private float tax_fee_amount;

    @Column(nullable = false)
    private float min_interest_bearing_amt;

    //REGULAR,TIME
    @Enumerated(EnumType.STRING)
    private ProductType product_type;

    //IB,IF
    @Enumerated(EnumType.STRING)
    private InterestType interest_type;

    //ACTIVE,DISABLED
    @Enumerated(EnumType.STRING)
    private ProductState state;

    @Column(nullable = false)
    private Integer days_for_inactivity;

    @Column(nullable = false)
    private Integer days_for_dormancy;

    @Column(nullable = false)
    private Integer financial_institution_id;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;

}
