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

@Table(name = "charges")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Charge {

    @Id
    @GeneratedValue(generator = "charges_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String charge_name;

    //Charge,Penality
    @Enumerated(EnumType.STRING)
    private ChargeType charge_type;

    //FLAT,PERCENTAGE
    @Enumerated(EnumType.STRING)
    private ChargeRate charge_calculation_type;

    //PERIODIC,ONETIME
    @Enumerated(EnumType.STRING)
    private ChargePaymentMode charge_payment_mode;

    //SAC, WDF, SAF, TRB, TRW, TRS, CAU
    @Enumerated(EnumType.STRING)
    private ChargeFor charge_for;

    //ACTIVE,INACTIVE
    @Enumerated(EnumType.STRING)
    private ChargeState status;

    @ManyToOne
    @JoinColumn(name = "saving_product_id")
    private Products products;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;

}
