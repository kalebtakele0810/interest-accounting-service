package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.constants.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "ledgers")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Transactions {

    @Id
    @GeneratedValue(generator = "ledgers_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private float bbt;

    @Column(nullable = false)
    private float bat;

    @Column(nullable = false)
    private float balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transaction_type;

    @Column(nullable = false)
    private String txn_id;

    @Column(nullable = false)
    private String txn_ref;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscriptions subscriptions;

    ///PENDING,SUCCESS,FAILED
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}
