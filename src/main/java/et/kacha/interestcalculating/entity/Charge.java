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

    @Column
    private String charge_name;

    //Charge,Penality
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeType charge_type;

    //FLAT,PERCENTAGE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeRate charge_calculation_type;

    //PERIODIC,ONETIME
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargePaymentMode charge_payment_mode;

    @Column
    private Integer charging_period;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private Actions actions;

    //SAC, WDF, SAF, TRB, TRW, TRS, CAU
    @Enumerated(EnumType.STRING)
    private ChargeFor charge_for;

    //ACTIVE,INACTIVE
    @Enumerated(EnumType.STRING)
    private ChargeState status;

    @ManyToOne
    @JoinColumn(name = "saving_product_id")
    private Products products;

    @Column(nullable = false)
    private Integer added_by;

    @Column
    private Integer approved_by;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}
