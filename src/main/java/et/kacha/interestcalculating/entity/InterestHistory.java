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
@Table(name = "interest_history")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class InterestHistory {
    @Id
    @GeneratedValue(generator = "interest_history_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscriptions subscriptions;

    @Column(nullable = true,name = "request_ref_id")
    private String requestRefId;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private Double interest_before_deduction;

    @Column(nullable = true)
    private Double interest_after_deduction;

    @Column(nullable = false)
    private Double interest_rate;

    @Enumerated(EnumType.STRING)
    private InterestPaymentState status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;
}
