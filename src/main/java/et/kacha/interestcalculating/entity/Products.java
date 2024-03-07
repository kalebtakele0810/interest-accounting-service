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

    @Column
    private String product_description;

    @Column
    private Integer decimal_places;

    @Column(nullable = false)
    private Float min_deposit_amt;

    @Column
    private Float max_deposit_amt;

    //IB,IF
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interest_type;

    @Column(nullable = false)
    private Float min_opening_balance;

    private Float max_saving_limit;

    @Column(nullable = false)
    private Integer days_for_inactivity;

    @Column(nullable = false)
    private Integer days_for_dormancy;

    @Column(nullable = false)
    private Integer term_duration;

    private Integer min_deposit_term;

    private Integer max_deposit_term;

    private Float min_interest_bearing_amt;

    //DAILY,MONTHLY,CUSTOM
    @Enumerated(EnumType.STRING)
    private InterestCompType interest_comp_type;

    @Column
    private Integer interest_posting_period;

    @Column(name = "is_ordinary")
    private Boolean isOrdinary;

    //MIN_BALANCE,AVERAGE
    @Enumerated(EnumType.STRING)
    private InterestCalculatedUsing interest_calculated_using;

    @Column(nullable = false)
    private Float interest_rate;

    //REGULAR,TIME
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType product_type;

    private Boolean is_tax_available;

    //FLAT,PERCENTAGE
    @Enumerated(EnumType.STRING)
    private ChargeRate tax_fee_type;

    private Float tax_fee_amount;

    @Column(nullable = false)
    private Integer added_by;

    @Column
    private Integer approved_by;

    //ACTIVE,DISABLED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState state;

    private Integer financial_institution_id;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}
