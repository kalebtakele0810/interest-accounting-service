package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "interest_fee_history")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class InterestFeeHistory {
    @Id
    @GeneratedValue(generator = "interest_fee_history_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "interest_history_id")
    private InterestHistory interestHistory;

    @ManyToOne
    @JoinColumn(name = "charge_fees_id")
    private ChargeFees chargeFees;

    @Column(nullable = false)
    private float amount;

    @Enumerated(EnumType.STRING)
    private InterestPaymentState status;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;
}
