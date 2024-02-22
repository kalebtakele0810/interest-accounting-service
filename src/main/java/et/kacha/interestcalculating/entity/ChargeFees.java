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

@Table(name = "charge_fees")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ChargeFees {

    @Id
    @GeneratedValue(generator = "charge_fees_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private float charge_amount;

    @Column
    private Float range_minimum;

    @Column
    private Float range_maximum;

    @ManyToOne
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;

    @Column(nullable = false)
    private Integer added_by;

    @Column
    private Integer approved_by;

    //ACTIVE,INACTIVE
    @Enumerated(EnumType.STRING)
    private ChargeState status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}
